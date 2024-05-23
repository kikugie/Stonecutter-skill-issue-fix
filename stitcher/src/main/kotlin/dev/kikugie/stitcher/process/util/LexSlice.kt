package dev.kikugie.stitcher.process.util

import dev.kikugie.stitcher.data.TokenType

data class LexSlice(
    val type: TokenType,
    val range: IntRange
)
