package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.parser.*
import dev.kikugie.stitcher.type.StitcherToken

class ConditionVisitor(private val processor: ExpressionProcessor) : Component.Visitor<Boolean> {
    override fun visitUnary(unary: Unary) = when(unary.operator.type) {
        StitcherToken.NEGATE -> !visitComponent(unary.target)
        else -> throw StitcherSyntaxException(unary.operator, "Unsupported unary operator")
    }

    override fun visitBinary(binary: Binary) = when(binary.operator.type) {
        StitcherToken.AND -> visitComponent(binary.left) && visitComponent(binary.right)
        StitcherToken.OR -> visitComponent(binary.left) || visitComponent(binary.right)
        else -> throw StitcherSyntaxException(binary.operator, "Unsupported binary operator")
    }

    override fun visitGroup(group: Group) = visitComponent(group.content)

    override fun visitEmpty(empty: Empty) = true

    override fun visitLiteral(literal: Literal) = processor.test(literal.token)

    override fun visitCondition(condition: Condition) = visitComponent(condition.condition)

    override fun visitSwap(swap: Swap) = throw UnsupportedOperationException()
}