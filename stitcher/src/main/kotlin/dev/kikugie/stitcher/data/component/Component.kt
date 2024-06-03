package dev.kikugie.stitcher.data.component

import dev.kikugie.stitcher.data.component.Component.Visitor
import dev.kikugie.semver.VersionPredicate
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.Token
import kotlinx.serialization.Serializable

/**
 * Structural node of Stitcher expressions.
 * Combines tokens or other components into meaningful structures.
 */
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

/**
 * Represents nothing. Used as a placeholder by the parser to fill in other components.
 */
@Serializable
data object Empty : Component {
    override fun isEmpty() = true
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitEmpty(this)
}

/**
 * Represents an endpoint value, such as a constant, version predicate or swap identifier.
 *
 * @property token assigned value
 */
@Serializable
data class Literal(val token: Token) : Component {
    override fun isEmpty(): Boolean = token.isBlank()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitLiteral(this)
}

/**
 * Represent a unary operation, such as (and currently only) negation `!{token}`
 *
 * @property operator token representing the type of the operation
 * @property target component this operation is applied to
 */
@Serializable
data class Unary(
    val operator: Token,
    val target: Component,
) : Component {
    override fun isEmpty(): Boolean = operator.isBlank() && target.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitUnary(this)
}

/**
 * Represents an operation with 2 parameters, such as `&&` and `||`.
 *
 * @property left left side of the operation
 * @property operator token representing the type of the operation
 * @property right right side of the operation
 */
@Serializable
data class Binary(
    val left: Component,
    val operator: Token,
    val right: Component,
) : Component {
    override fun isEmpty(): Boolean = left.isEmpty() && operator.isBlank() && right.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitBinary(this)
}

/**
 * Represents a component encased in parentheses.
 *
 * @property content delegate component
 */
@Serializable
data class Group(
    val content: Component,
) : Component {
    override fun isEmpty(): Boolean = content.isEmpty()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitGroup(this)
}

/**
 * Represents a version comparison.
 *
 * With explicit target it is written as `identifier: 0.1.0`.
 * For the default value, assignment operator `:` may be emitted: `0.1.0`
 *
 * @property target identifier of the dependency or [Token.EMPTY] for implicit assignments
 * @property predicates a list of tokens, parsed as a [VersionPredicate]
 */
@Serializable
data class Assignment(
    val target: Token,
    val predicates: List<Token>,
) : Component {
    override fun isEmpty(): Boolean = target.isBlank() && !predicates.any { !it.isBlank() }
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignment(this)
}

/**
 * Top level component of conditions and swaps, encoding the entire content of the original comment.
 *
 * @property component [Condition] or [Swap] component, containing the parsed data
 * @property extension whenever this comment starts with `}`
 * @property enclosure the type of the next [Scope]
 */
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

/**
 * Represents Stitcher condition expression.
 *
 * @property sugar condition sugar added for readability, such as `if`, `else` and `elif`, which is useless for the transformer, but required to reassemble the tree.
 * @property condition underlying condition tree
 */
@Serializable
data class Condition(
    val sugar: List<Token> = listOf(),
    val condition: Component = Empty,
) : Component {
    override fun isEmpty(): Boolean = condition.isEmpty() && !sugar.any { !it.isBlank() }
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitCondition(this)
}

/**
 * Represents Stitcher swap comment.
 *
 * @property identifier assigned swap id
 */
@Serializable
data class Swap(
    val identifier: Token,
) : Component {
    override fun isEmpty(): Boolean = identifier.isBlank()
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitSwap(this)
}