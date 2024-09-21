package dev.kikugie.stitcher.transformer

import dev.kikugie.semver.VersionPredicate
import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.token.StitcherTokenType
import dev.kikugie.stitcher.data.token.Token

/**
 * Evaluates the passed [Component]'s boolean value.
 *
 * @property params Input parameters for the used identifiers
 */
class ConditionChecker(private val params: TransformParameters) : Component.Visitor<Boolean> {
    override fun visitEmpty(it: Empty): Boolean = true
    override fun visitGroup(it: Group): Boolean = it.content.accept(this)
    override fun visitDefinition(it: Definition): Boolean = it.component.accept(this)
    override fun visitCondition(it: Condition): Boolean = it.condition.accept(this)

    @Throws(UnsupportedOperationException::class)
    override fun visitSwap(it: Swap): Boolean {
        throw UnsupportedOperationException("Swaps can't be processed as conditions")
    }

    @Throws(IllegalArgumentException::class)
    override fun visitUnary(it: Unary): Boolean = when (it.operator.type) {
        StitcherTokenType.NEGATE -> !it.target.accept(this)
        else -> throw IllegalArgumentException("Invalid operator ${it.operator.type} '${it.operator.value}'")
    }

    @Throws(IllegalArgumentException::class)
    override fun visitBinary(it: Binary): Boolean = when (it.operator.type) {
        StitcherTokenType.AND -> it.left.accept(this) && it.right.accept(this)
        StitcherTokenType.OR -> it.left.accept(this) || it.right.accept(this)
        else -> throw IllegalArgumentException("Invalid operator ${it.operator.type} '${it.operator.value}'")
    }

    override fun visitLiteral(it: Literal): Boolean = params.constants[it.token.value]
        ?: Assignment(Token.EMPTY, listOf(it.token)).accept(this)

    override fun visitAssignment(it: Assignment): Boolean {
        val target = params.dependencies[it.target.value]
            ?: throw IllegalArgumentException("Invalid dependency ${it.target.value}")
        return it.predicates.all {
            val info = it[VersionPredicate::class] ?: VersionPredicate.parseLenient(it.value)
            info.eval(target)
        }
    }
}