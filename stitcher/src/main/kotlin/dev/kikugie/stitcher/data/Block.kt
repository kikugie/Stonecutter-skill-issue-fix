package dev.kikugie.stitcher.data

import dev.kikugie.stitcher.data.Block.Visitor
import kotlinx.serialization.Serializable

@Serializable
sealed interface Block {
    fun <T> accept(visitor: Visitor<T>): T
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean = !isEmpty()

    interface Visitor<T> {
        fun visitContent(it: ContentBlock): T
        fun visitComment(it: CommentBlock): T
        fun visitCode(it: CodeBlock): T
    }
}

@Serializable
data class ContentBlock(
    val content: Token,
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitContent(this)
    override fun isEmpty(): Boolean = content.value.isBlank()
}

@Serializable
data class CommentBlock(
    val start: Token,
    val content: Token,
    val end: Token
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitComment(this)
    override fun isEmpty(): Boolean = false
}

@Serializable
data class CodeBlock(
    val start: Token,
    val def: Definition,
    val end: Token,
    val scope: Scope? = null,
) : Block {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCode(this)
    override fun isEmpty(): Boolean = false
}