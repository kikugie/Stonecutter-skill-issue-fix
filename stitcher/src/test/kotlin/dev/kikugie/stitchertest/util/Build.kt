package dev.kikugie.stitchertest.util

import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitcher.type.TokenType

fun MutableList<Token>.token(value: String, type: TokenType) {
    add(Token(value, type))
}

