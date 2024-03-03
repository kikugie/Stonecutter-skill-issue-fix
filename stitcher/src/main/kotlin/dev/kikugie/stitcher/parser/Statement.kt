package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.token.EOF
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.token.TokenType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface Statement {
    fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitContent(it: ContentBlock): T
        fun visitComment(it: CommentBlock): T
        fun visitScope(it: Scope): T
    }
}

@Serializable
sealed interface Block

@Serializable
data class ContentBlock(
    val token: Token
) : Statement, Block {
    override fun <T> accept(visitor: Statement.Visitor<T>) = visitor.visitContent(this)
}

@Serializable
data class CommentBlock(
    val start: Token,
    val content: Component,
    val end: Token,
) : Statement, Block {
    override fun <T> accept(visitor: Statement.Visitor<T>) = visitor.visitComment(this)
}

@Serializable
data class Scope(
    val type: TokenType = EOF,
    val enclosure: ScopeType = ScopeType.CLOSED,
    val blocks: MutableList<Block> = mutableListOf()
) : Statement {
    fun add(block: Block) {
        blocks.add(block)
    }

    fun remove(block: Block) {
        blocks.remove(block)
    }

    fun replace(source: Block, block: Block) {
        blocks[blocks.indexOf(source)] = block
    }

    fun find(predicate: (Block) -> Boolean): Block? {
        return blocks.find(predicate)
    }

    override fun <T> accept(visitor: Statement.Visitor<T>) = visitor.visitScope(this)
}