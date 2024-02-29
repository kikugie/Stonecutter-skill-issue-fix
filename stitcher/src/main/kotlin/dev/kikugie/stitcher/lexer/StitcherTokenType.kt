package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.token.TokenType
import kotlinx.serialization.Serializable

@Serializable
enum class StitcherTokenType : TokenType {
    // Single-char
    CONDITION, SWAP, // ? and $
    BLOCK_OPEN, BLOCK_CLOSE,  // { and }

    // Multi-char
    AND, OR, IF, ELSE, ELIF, // && , || and self-explanatory
    EXPRESSION, // anything in-between is sent to the expression processor
}