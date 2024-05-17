package dev.kikugie.stitcher.process.access

class SwapProcessor(
    private val swaps: Swaps,
    private val checker: ExpressionProcessor
) {
    fun get(identifier: String) = swaps[identifier]?.invoke(checker) ?: throw IllegalArgumentException("Invalid id: $identifier")
}