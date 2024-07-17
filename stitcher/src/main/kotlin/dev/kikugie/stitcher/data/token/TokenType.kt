package dev.kikugie.stitcher.data.token

import kotlinx.serialization.Serializable

@Serializable
sealed interface TokenType

@Serializable
data object NullType : TokenType
data object WhitespaceType : TokenType

@Serializable
enum class ContentType : TokenType {
    COMMENT_START,
    COMMENT_END,
    COMMENT,
    CONTENT
}

enum class MarkerType : TokenType {
    CONDITION, SWAP, // ? and $
}

@Serializable
enum class StitcherTokenType : TokenType {
    // Single-char
    SCOPE_OPEN, SCOPE_CLOSE,  // { and }
    GROUP_OPEN, GROUP_CLOSE, // ( and )
    NEGATE, // !
    ASSIGN, // :

    // Multi-char
    AND, OR, IF, ELSE, ELIF, // && , || and self-explanatory
    EXPECT_WORD, // >>
    IDENTIFIER,
    PREDICATE,
}