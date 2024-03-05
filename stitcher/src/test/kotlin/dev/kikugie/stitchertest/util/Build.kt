package dev.kikugie.stitchertest.util

import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.TokenType

fun MutableList<Token>.token(value: String, range: IntRange, type: TokenType) {
    add(Token(value, range, type))
}

