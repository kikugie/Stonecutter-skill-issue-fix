package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.eval.isBlank
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stitcher.eval.join

object CommentRemover : Block.Visitor<CharSequence>, Scope.Visitor<String?> {
    override fun visitCode(it: CodeBlock): CharSequence = unmap(it.join())
    override fun visitContent(it: ContentBlock) = unmap(it.content)
    override fun visitComment(it: CommentBlock) = unmap(it.content).apply {
        if (it.end.isBlank()) append(it.end.value)
    }

    override fun visitScope(it: Scope): String? = when(it.enclosure) {
        ScopeType.CLOSED -> if (!it.isCommented()) null else it.joinToString("") {
            it.accept(this)
        }
        else -> it.indexOfFirst { it.isNotEmpty() }.takeIf { it >= 0 }?.let { i ->
            val block = it[i].takeIf { it is CommentBlock } ?: return@let null
            it[i] = ContentBlock(block.accept(this))
            it.join()
        }
    }

    private fun unmap(token: Token) = unmap(token.value)
    private fun unmap(str: CharSequence) = remap(str, '^', '*', false)
}