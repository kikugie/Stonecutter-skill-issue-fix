package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.parser.Component.*
import dev.kikugie.stitcher.token.Token
import kotlinx.serialization.Serializable

@Serializable
sealed interface Component {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitBinary(binary: Binary): T
        fun visitGroup(group: Group): T
        fun visitEmpty(empty: Empty): T
        fun visitLiteral(literal: Literal): T
        fun visitCondition(condition: Condition): T
        fun visitSwap(swap: Swap): T
    }
}

@Serializable
data object Empty : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitEmpty(this)
}

@Serializable
data class Literal(val token: Token) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitLiteral(this)
}

@Serializable
data class Binary(
    val left: Component,
    val operator: Token,
    val right: Component,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitBinary(this)
}

@Serializable
data class Group(
    val content: Component
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitGroup(this)
}

@Serializable
data class Condition(
    val sugar: List<Token>,
    val condition: Component,
    val extension: Boolean,
    val then: Scope,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitCondition(this)
}

@Serializable
data class Swap(
    val identifier: Token,
    val scope: Scope,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitSwap(this)
}