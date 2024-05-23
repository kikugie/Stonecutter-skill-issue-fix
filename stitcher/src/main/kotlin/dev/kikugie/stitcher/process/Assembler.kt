package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.data.StitcherTokenType

@Suppress("MemberVisibilityCanBePrivate")
object Assembler : Component.Visitor<String>, Block.Visitor<String>, Scope.Visitor<String> {
    private fun StringBuilder.token(token: Token): StringBuilder = append(token.value)

    private fun StringBuilder.space(): StringBuilder = append(' ')

    private fun StringBuilder.appendVisit(it: Component) = append(it.accept(Assembler))
    private fun StringBuilder.appendVisit(it: Block) = append(it.accept(Assembler))
    private fun StringBuilder.appendVisit(it: Scope) = append(it.accept(Assembler))

    override fun visitEmpty(it: Empty) = ""
    override fun visitLiteral(it: Literal) = it.token.value
    override fun visitUnary(it: Unary): String = buildString {
        append(it.operator.value)
        appendVisit(it.target)
    }

    override fun visitBinary(it: Binary) = buildString {
        appendVisit(it.left)
        space()
        token(it.operator)
        space()
        appendVisit(it.right)
    }

    override fun visitGroup(it: Group) = buildString {
        append('(')
        appendVisit(it.content)
        append(')')
    }

    override fun visitCondition(it: Condition) = buildString {
        val components = it.sugar.map { it.value }.toMutableList()
        it.condition.accept(this@Assembler).run {
            if (isNotBlank()) components.add(this)
        }
        if (components.isNotEmpty())
            append(components.joinToString(" "))
    }

    override fun visitSwap(it: Swap) = buildString {
        if (!it.identifier.isBlank()) token(it.identifier)
    }

    override fun visitDefinition(it: Definition) = buildString {
        when (it.type) {
            MarkerType.CONDITION -> append('?')
            MarkerType.SWAP -> append('$')
            else -> {}
        }
        if (it.extension) append('}')
        val component = it.component.accept(this@Assembler)
        if (component.isNotBlank()) {
            space()
            append(component)
        }
        if (it.enclosure != ScopeType.LINE) {
            space()
            append(it.enclosure.id)
        }
    }

    override fun visitAssignment(it: Assignment) = buildString {
        token(it.target)
        append(':')
        for (predicate in it.predicates) {
            space()
            token(predicate)
        }
    }

    override fun visitContent(it: ContentBlock) = buildString {
        token(it.content)
    }

    override fun visitComment(it: CommentBlock) = buildString {
        token(it.start)
        token(it.content)
        token(it.end)
    }

    override fun visitCode(it: CodeBlock) = buildString {
        token(it.start)
        appendVisit(it.def)
        token(it.end)
    }

    override fun visitScope(it: Scope): String = buildString {
        it.forEach { bl -> appendVisit(bl) }
    }
}