package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.TokenType

data class LexSlice(
    val type: TokenType,
    val range: IntRange,
    val source: CharSequence
) {
    constructor(type: TokenType, range: Int, source: CharSequence) : this(type, range..range, source)

    val value get() = if (range.first >= 0 && range.last < source.length) source.substring(range) else ""
    val token get() = Token(value, type)

    override fun toString(): String = "LexSlice(type=$type, range=${range.first}..<${range.last + 1}, value=$value)"

    companion object {
        val EMPTY = LexSlice(NullType, 0..0, "")
    }
}