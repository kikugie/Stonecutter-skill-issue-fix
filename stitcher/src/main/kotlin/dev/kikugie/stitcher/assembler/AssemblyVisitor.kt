package dev.kikugie.stitcher.assembler

import dev.kikugie.stitcher.parser.*
import dev.kikugie.stitcher.token.Token

@Suppress("MemberVisibilityCanBePrivate")
object AssemblyVisitor : Component.Visitor<String>, Block.Visitor<String> {
    private fun StringBuilder.token(token: Token): StringBuilder = append(token.value)

    private fun StringBuilder.space(): StringBuilder = append(' ')

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
        val components = condition.sugar.map { it.value }.toMutableList()
        visitComponent(condition.condition).run {
            if (isNotBlank()) components.add(this)
        }
        if (components.isNotEmpty()) {
            space()
            append(components.joinToString(" "))
        }
    }

    override fun visitSwap(swap: Swap) = buildString {
        append('$')
        if (swap.extension)
            append('}')
        else {
            space()
            token(swap.identifier)
        }
    }

    override fun visitContent(content: ContentBlock) = content.token.value

    override fun visitComment(comment: CommentBlock) = buildString {
        token(comment.start)
        append(visitComponent(comment.content))
        when(val enclosure = comment.scope?.enclosure) {
            ScopeType.CLOSED, ScopeType.WORD -> {
                space()
                append(enclosure.id)
            }
            else -> {}
        }
        token(comment.end)
        if (comment.scope != null)
            append(visitScope(comment.scope))
    }

    fun visitScope(scope: Scope): String = buildString {
        for (it in scope.blocks) append(visitBlock(it))
    }
}