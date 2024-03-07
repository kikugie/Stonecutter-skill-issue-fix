package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.assembler.AssemblyVisitor
import dev.kikugie.stitcher.parser.Component.*
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.StitcherToken.*
import kotlinx.serialization.Serializable

/**
 * Represents a component in a Stitcher program.
 *
 * Components are stored inside blocks and can be nested to form a tree-like structure.
 * @see Block
 */
@Serializable
sealed interface Component {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitComponent(it: Component) = when(it) {
            is Empty -> visitEmpty(it)
            is Literal -> visitLiteral(it)
            is Unary -> visitUnary(it)
            is Binary -> visitBinary(it)
            is Group -> visitGroup(it)
            is Condition -> visitCondition(it)
            is Swap -> visitSwap(it)
        }

        fun visitUnary(unary: Unary): T
        fun visitBinary(binary: Binary): T
        fun visitGroup(group: Group): T
        fun visitEmpty(empty: Empty): T
        fun visitLiteral(literal: Literal): T
        fun visitCondition(condition: Condition): T
        fun visitSwap(swap: Swap): T
    }
}

/**
 * A placeholder that indicates an empty or null value.
 *
 * @see Component
 */
@Serializable
data object Empty : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitEmpty(this)
}

/**
 * Wrapper for a single token, usually an [EXPRESSION].
 *
 * @property token The token representing the literal value.
 *
 * @see Token
 * @see Component
 */
@Serializable
data class Literal(val token: Token) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitLiteral(this)
}

/**
 * Represents a unary operation in a Stitcher program.
 *
 * Available unary operators are: [NEGATE]
 *
 * @param operator The operator token defining the unary operation.
 * @param target The target component of the unary operation.
 *
 * @see Component
 */
@Serializable
data class Unary(
    val operator: Token,
    val target: Component,
) : Component {
    override fun <T> accept(visitor: Visitor<T>)= visitor.visitUnary(this)
}

/**
 * Represents a binary operation in a Stitcher program.
 *
 * Available operations are [OR] and [AND]
 *
 * @param left The left component of the binary operation.
 * @param operator The operator token defining the binary operation.
 * @param right The right component of the binary operation.
 *
 * @see Component
 */
@Serializable
data class Binary(
    val left: Component,
    val operator: Token,
    val right: Component,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitBinary(this)
}

/**
 * Represents a group component enclosed in parentheses.
 *
 * @param content The component contained within the group.
 *
 * @see Component
 */
@Serializable
data class Group(
    val content: Component
) : Component {
    override fun <T> accept(visitor: Visitor<T>)= visitor.visitGroup(this)
}

/**
 * Represents a condition component in a Stitcher program.
 *
 * Conditions determine whenever the comment's scope should commented or not.
 *
 * @property sugar The list of sugar tokens associated with the condition.
 * Sugar tokens are additional tokens that appear before the condition expression.
 * These are preserved for the assembler, so it doesn't yeet all your readability.
 * @property condition The condition expression.
 * @property extension A flag indicating whether the condition is an extension of a previous condition.
 *
 * @see Component
 */
@Serializable
data class Condition(
    val sugar: List<Token> = listOf(),
    val condition: Component = Empty,
    val extension: Boolean = false,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitCondition(this)
}

/**
 * Represents a swap component in a Stitcher program.
 *
 * Swaps are used to replace blocks of code with a pre-set value.
 * These are introduced as a shortcut for the following:
 * ```kt
 *  //? if expr {
 *  func1()
 *  //?} else {
 *  /*func2()*/
 *  //?}
 *
 *  // ... the same blocks
 * ```
 * If such check is often and contains the same scopes, it may be replaced with a swap:
 * ```kt
 *  //$ token {
 *  func1()
 *  //$}
 * ```
 * Substitutions are defined externally before the compilation, but are allowed to statically execute a condition.
 *
 * @param identifier The identifier token for the substitution lookup.
 *
 * @see Component
 */
@Serializable
data class Swap(
    val identifier: Token = Token.eof(-1),
    val extension: Boolean = false,
) : Component {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitSwap(this)
}