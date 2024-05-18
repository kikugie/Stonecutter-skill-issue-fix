package dev.kikugie.stitcher.process.transformer

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitcher.process.access.ExpressionProcessor
import dev.kikugie.stitcher.type.StitcherToken.*

class ConditionVisitor(private val processor: ExpressionProcessor) : Component.Visitor<Boolean> {
    override fun visitUnary(unary: Unary) = when (unary.operator.type) {
        NEGATE -> !visitComponent(unary.target)
        else -> throw SyntaxException("Unsupported unary operator: ${unary.operator.value}")
    }

    override fun visitBinary(binary: Binary) = when (binary.operator.type) {
        AND -> visitComponent(binary.left) && visitComponent(binary.right)
        OR -> visitComponent(binary.left) || visitComponent(binary.right)
        else -> throw SyntaxException("Unsupported binary operator: ${binary.operator.value}")
    }

    override fun visitGroup(group: Group) = visitComponent(group.content)

    override fun visitEmpty(empty: Empty) = true // Else case

    override fun visitLiteral(literal: Literal) = processor.test(literal.token.value)

    override fun visitCondition(condition: Condition) = visitComponent(condition.condition)

    override fun visitSwap(swap: Swap) = throw UnsupportedOperationException()
}