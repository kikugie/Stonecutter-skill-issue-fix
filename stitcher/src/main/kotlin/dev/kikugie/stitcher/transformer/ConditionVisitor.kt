package dev.kikugie.stitcher.transformer

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.semver.VersionComparisonOperator
import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength
import dev.kikugie.semver.VersionPredicate
import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.token.StitcherTokenType
import dev.kikugie.stitcher.data.token.Token

/**
 * Evaluates the passed [Component]s boolean value.
 *
 * @property params Input parameters for the used identifiers
 */
class ConditionVisitor(private val params: TransformParameters) : Component.Visitor<Boolean> {
    override fun visitUnary(it: Unary): Boolean = when(it.operator.type) {
        StitcherTokenType.NEGATE -> !it.target.accept(this)
        else -> throw IllegalArgumentException("Invalid operator ${it.operator.type} (${it.operator.value}")
    }

    override fun visitBinary(it: Binary): Boolean = when(it.operator.type) {
        StitcherTokenType.AND -> it.left.accept(this) && it.right.accept(this)
        StitcherTokenType.OR -> it.left.accept(this) || it.right.accept(this)
        else -> throw IllegalArgumentException("Invalid operator ${it.operator.type} (${it.operator.value}")
    }

    override fun visitGroup(it: Group): Boolean = it.content.accept(this)

    override fun visitEmpty(it: Empty): Boolean = true

    override fun visitLiteral(it: Literal): Boolean =
        params.constants[it.token.value] ?: Assignment(Token.EMPTY, listOf(it.token)).accept(this)

    override fun visitCondition(it: Condition): Boolean = it.condition.accept(this)

    /**
     * @throws UnsupportedOperationException
     */
    override fun visitSwap(it: Swap): Boolean {
        throw UnsupportedOperationException("Swaps can't be processed as conditions")
    }

    override fun visitDefinition(it: Definition): Boolean = it.component.accept(this)

    override fun visitAssignment(it: Assignment): Boolean {
        val target = params.dependencies[it.target.value] ?: throw IllegalArgumentException()
        return it.predicates.all {
            val info = it[VersionPredicate::class] ?: run {
                val str = it.value
                val len = str.operatorLength()
                val op = if (len == 0) VersionComparisonOperator.EQUAL
                else VersionComparisonOperator.match(str.substring(0, len))
                val ver = SemanticVersionParser.parse(str.substring(len))
                VersionPredicate(op, ver)
            }
            info.operator(target, info.version)
        }
    }
}