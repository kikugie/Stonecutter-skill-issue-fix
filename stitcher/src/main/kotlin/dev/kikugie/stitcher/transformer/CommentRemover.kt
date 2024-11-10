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

    override fun visitScope(it: Scope): String? = when (it.enclosure) {
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
    private fun unmap(str: CharSequence) = StringBuilder(str).apply {
        var index = 0
        while (true) {
            index = indexOf('^', index)
            if (index < 0) break
            else if (charMatches(index - 1, '/')) when (val depth = readSuperScript(index + 1)) {
                0, 1 -> {
                    if (depth == 0) this[index] = '*'
                    removeSuperScript(index + 1)
                }

                else -> writeSuperScriptAfter(index, depth - 1)
            }
            else if (charMatches(index + 1, '/')) when (val depth = readSuperScript(index - 1)) {
                0, 1 -> {
                    if (depth == 0) this[index] = '*'
                    if (removeSuperScript(index - 1)) index++
                }

                else -> writeSuperScriptBefore(index, depth - 1)
            }
            index++
        }
    }
}