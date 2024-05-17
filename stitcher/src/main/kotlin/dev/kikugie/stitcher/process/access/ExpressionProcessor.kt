package dev.kikugie.stitcher.process.access

import dev.kikugie.stitcher.util.memoize

class ExpressionProcessor(
    private val constants: Constants = emptyMap(),
    private val expressions: Expressions = emptyList()
) {
    val cache = memoize<String, Boolean?> {
        constants[it] ?: expressions.firstNotNullOfOrNull { e -> e(it) }
    }

    fun test(expression: String): Boolean = cache(expression) ?: throw IllegalArgumentException("Invalid expression: $expression")
}