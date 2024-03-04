package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.parser.Block.Visitor
import dev.kikugie.stitcher.token.Token
import kotlinx.serialization.Serializable

@Serializable
sealed interface Block {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitContent(content: ContentBlock): T
        fun visitComment(comment: CommentBlock): T
    }
}

@Serializable
data class ContentBlock(
    val token: Token
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitContent(this)
}

@Serializable
data class CommentBlock(
    val start: Token,
    val content: Component,
    val end: Token,
    val scope: Scope? = null
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitComment(this)
}