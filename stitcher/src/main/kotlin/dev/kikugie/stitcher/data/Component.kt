package dev.kikugie.stitcher.data

import dev.kikugie.stitcher.data.Component.Visitor
import kotlinx.serialization.Serializable

@Serializable
sealed interface Component {
    fun isEmpty(): Boolean
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitUnary(it: Unary): T
        fun visitBinary(it: Binary): T
        fun visitGroup(it: Group): T
        fun visitEmpty(it: Empty): T
        fun visitLiteral(it: Literal): T
        fun visitCondition(it: Condition): T
        fun visitSwap(it: Swap): T
        fun visitDefinition(it: Definition): T
        fun visitAssignment(it: Assignment): T
    }
}

@Serializable
data object Empty : Component {
    override fun isEmpty() = true
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitEmpty(this)
}

@Serializable
data class Literal(val token: Token) : Component {
    override fun isEmpty(): Boolean = token.isBlank()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitLiteral(this)
}

@Serializable
data class Unary(
    val operator: Token,
    val target: Component,
) : Component {
    override fun isEmpty(): Boolean = operator.isBlank() && target.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitUnary(this)
}

@Serializable
data class Binary(
    val left: Component,
    val operator: Token,
    val right: Component,
) : Component {
    override fun isEmpty(): Boolean = left.isEmpty() && operator.isBlank() && right.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitBinary(this)
}

@Serializable
data class Group(
    val content: Component,
) : Component {
    override fun isEmpty(): Boolean = content.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitGroup(this)
}

@Serializable
data class Assignment(
    val target: Token,
    val predicates: List<Token>,
) : Component {
    override fun isEmpty(): Boolean = target.isBlank() && !predicates.any { !it.isBlank() }
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignment(this)
}

@Serializable
data class Definition(
    val component: Component,
    val extension: Boolean = false,
    val enclosure: ScopeType = ScopeType.LINE,
) : Component {
    val type
        get() = when (component) {
            is Condition -> MarkerType.CONDITION
            is Swap -> MarkerType.SWAP
            else -> null
        }

    override fun isEmpty(): Boolean = component.isEmpty() && enclosure == ScopeType.LINE
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitDefinition(this)
}

@Serializable
data class Condition(
    val sugar: List<Token> = listOf(),
    val condition: Component = Empty,
) : Component {
    override fun isEmpty(): Boolean = condition.isEmpty() && !sugar.any { !it.isBlank() }
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitCondition(this)
}

@Serializable
data class Swap(
    val identifier: Token,
) : Component {
    override fun isEmpty(): Boolean = identifier.isBlank()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitSwap(this)
}