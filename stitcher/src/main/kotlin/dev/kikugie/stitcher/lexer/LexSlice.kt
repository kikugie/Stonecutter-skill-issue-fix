package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.TokenType

data class LexSlice(
    val type: TokenType,
    val range: IntRange
)
