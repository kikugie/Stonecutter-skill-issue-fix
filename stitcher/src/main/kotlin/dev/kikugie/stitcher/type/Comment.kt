package dev.kikugie.stitcher.type

import kotlinx.serialization.Serializable

@Serializable
enum class Comment : TokenType {
    COMMENT_START,
    COMMENT_END,
    COMMENT,
    CONTENT
}