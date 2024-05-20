package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*

@Suppress("MemberVisibilityCanBePrivate")
object Assembler : Component.Visitor<String>, Block.Visitor<String>, Scope.Visitor<String> {
    private fun StringBuilder.token(token: Token): StringBuilder = append(token.value)

    private fun StringBuilder.space(): StringBuilder = append(' ')

    private fun StringBuilder.appendVisit(it: Component) = append(it.accept(Assembler))
    private fun StringBuilder.appendVisit(it: Block) = append(it.accept(Assembler))
    private fun StringBuilder.appendVisit(it: Scope) = append(it.accept(Assembler))

    override fun visitEmpty(empty: Empty) = ""

    override fun visitLiteral(literal: Literal) = literal.token.value

    override fun visitUnary(unary: Unary): String = buildString {
        append(unary.operator.value)
        appendVisit(unary.target)
    }

    override fun visitBinary(binary: Binary) = buildString {
        appendVisit(binary.left)
        space()
        token(binary.operator)
        space()
        appendVisit(binary.right)
    }

    override fun visitGroup(group: Group) = buildString {
        append('(')
        appendVisit(group.content)
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
        appendVisit(comment.content)
        val enclosure = comment.scope?.enclosure
        if (enclosure != null) space()
        when (enclosure) {
            ScopeType.CLOSED, ScopeType.WORD -> append(enclosure.id)
            else -> {}
        }
        token(comment.end)
        if (comment.scope != null)
            appendVisit(comment.scope)
    }

    override fun visitScope(scope: Scope): String = buildString {
        for (it in scope.blocks) appendVisit(it)
    }
}