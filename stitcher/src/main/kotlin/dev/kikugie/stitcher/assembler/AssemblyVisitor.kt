package dev.kikugie.stitcher.assembler

import dev.kikugie.stitcher.parser.*
import dev.kikugie.stitcher.token.Token

@Suppress("MemberVisibilityCanBePrivate")
object AssemblyVisitor : Component.Visitor<String>, Block.Visitor<String> {
    private fun StringBuilder.token(token: Token): StringBuilder = append(token.value)

    private fun StringBuilder.space(): StringBuilder = append(' ')

    fun visitComponent(it: Component): String = when(it) {
        is Empty -> visitEmpty(it)
        is Literal -> visitLiteral(it)
        is Unary -> visitUnary(it)
        is Binary -> visitBinary(it)
        is Group -> visitGroup(it)
        is Condition -> visitCondition(it)
        is Swap -> visitSwap(it)
    }

    fun visitBlock(it: Block) = when(it) {
        is CommentBlock -> visitComment(it)
        is ContentBlock -> visitContent(it)
    }

    override fun visitEmpty(empty: Empty) = ""

    override fun visitLiteral(literal: Literal) = literal.token.value

    override fun visitUnary(unary: Unary): String = unary.operator.value + visitComponent(unary.target)

    override fun visitBinary(binary: Binary) = buildString {
        append(visitComponent(binary.left))
        space()
        token(binary.operator)
        space()
        append(visitComponent(binary.right))
    }

    override fun visitGroup(group: Group) = buildString {
        append('(')
        append(visitComponent(group.content))
        append(')')
    }

    override fun visitCondition(condition: Condition) = buildString {
        append('?')
        if (condition.extension)
            append('}')
        space()
        val components = condition.sugar.map { it.value }.toMutableList()
        visitComponent(condition.condition).run {
            if (isNotBlank()) components.add(this)
        }
        append(components.joinToString(" "))
    }

    override fun visitSwap(swap: Swap) = buildString {
        append('$')
        space()
        token(swap.identifier)
    }

    override fun visitContent(content: ContentBlock) = content.token.value

    override fun visitComment(comment: CommentBlock) = buildString {
        token(comment.start)
        append(visitComponent(comment.content))
        if (comment.scope?.enclosure == ScopeType.CLOSED) {
            space()
            append('{')
        }
        token(comment.end)
        if (comment.scope != null)
            append(visitScope(comment.scope))
    }

    fun visitScope(scope: Scope): String = buildString {
        scope.blocks.forEach {
            append(visitBlock(it))
        }
    }
}