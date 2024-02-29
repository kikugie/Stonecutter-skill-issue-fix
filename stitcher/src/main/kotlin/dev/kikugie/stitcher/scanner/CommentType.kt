package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.token.TokenType
import kotlinx.serialization.Serializable

@Serializable
enum class CommentType : TokenType {
    COMMENT_START,
    COMMENT_END,
    COMMENT,
    CONTENT
}