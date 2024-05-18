package dev.kikugie.stitcher.process.access

class SwapProcessor(
    private val swaps: Swaps,
) {
    fun get(identifier: String) = swaps[identifier] ?: throw IllegalArgumentException("Invalid id: $identifier")
}