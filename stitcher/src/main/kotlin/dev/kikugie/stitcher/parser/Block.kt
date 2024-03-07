package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.assembler.AssemblyVisitor
import dev.kikugie.stitcher.parser.Block.Visitor
import dev.kikugie.stitcher.token.Token
import kotlinx.serialization.Serializable

/**
 * Represents a block in the Stitcher program.
 *
 * @see ContentBlock
 * @see CommentBlock
 */
@Serializable
sealed interface Block {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitBlock(it: Block) = when(it) {
            is CommentBlock -> visitComment(it)
            is ContentBlock -> visitContent(it)
        }

        fun visitContent(content: ContentBlock): T
        fun visitComment(comment: CommentBlock): T
    }
}

/**
 * Represents a content block in a Stitcher program.
 *
 * A content block is a block of content without any special meaning.
 *
 * @property token The token representing the content of the block.
 *
 * @see Token
 * @see Block
 */
@Serializable
data class ContentBlock(
    val token: Token
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitContent(this)
}

/**
 * Represents a comment block in a Stitcher program.
 *
 * Comment blocks are a type of block that contain comments,
 * which may or may not have a meaning.
 *
 * @property start The token that represents the start of the comment block.
 * @property content The component that represents the content of the comment block.
 * @property end The comment that represents the end of the comment block.
 * @property scope The optional scope associated with the comment block.
 *
 * @see Token
 * @see Component
 * @see Scope
 */
@Serializable
data class CommentBlock(
    val start: Token,
    val content: Component,
    val end: Token,
    val scope: Scope? = null
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitComment(this)
}