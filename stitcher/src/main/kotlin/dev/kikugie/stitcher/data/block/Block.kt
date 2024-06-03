package dev.kikugie.stitcher.data.block

import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.block.Block.Visitor
import kotlinx.serialization.Serializable

/**
 * Base building block of the produced AST.
 * Uses inherited types to define the kind of the block and what kind of data they store.
 */
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

/**
 * Literal content of the processed file, i.e. the stuff in-between comments.
 *
 * @property content Content value
 */
@Serializable
data class ContentBlock(
    val content: Token,
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitContent(this)
    override fun isEmpty(): Boolean = content.value.isBlank()
}

/**
 * Regular comment, not recognized to have any Stitcher expressions.
 * Comment start and end tokens are stored separately to allow easily extracting contents.
 *
 * @property start Comment start marker
 * @property content Content value
 * @property end Comment end marker
 */
@Serializable
data class CommentBlock(
    val start: Token,
    val content: Token,
    val end: Token
) : Block {
    override fun <T> accept(visitor: Visitor<T>) = visitor.visitComment(this)
    override fun isEmpty(): Boolean = false
}

/**
 * Comment recognized as a Stitcher expression.
 * Stores the parsed code.
 *
 * @property start Comment start marker
 * @property def Parsed Stitcher code
 * @property end Comment end marker
 * @property scope Scope assigned to the block, or `null` if this is a closer
 */
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