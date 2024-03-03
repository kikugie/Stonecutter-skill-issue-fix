package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.token.TokenType
import kotlinx.serialization.Serializable

@Serializable
enum class StitcherTokenType : TokenType {
    // Single-char
    CONDITION, SWAP, // ? and $
    SCOPE_OPEN, SCOPE_CLOSE,  // { and }
    GROUP_OPEN, GROUP_CLOSE, // ( and )
    NEGATE, // !

    // Multi-char
    AND, OR, IF, ELSE, // && , || and self-explanatory
    EXPRESSION, // anything in-between is sent to the expression processor
}