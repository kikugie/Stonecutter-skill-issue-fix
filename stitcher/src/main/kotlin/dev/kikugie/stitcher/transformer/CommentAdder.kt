package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.eval.join

object CommentAdder : Block.Visitor<CharSequence>, Scope.Visitor<String?> {
    override fun visitCode(it: CodeBlock): CharSequence = map(it.join())
    override fun visitContent(it: ContentBlock): CharSequence = map(it.content)
    override fun visitComment(it: CommentBlock): CharSequence = map(it.join())

    override fun visitScope(it: Scope): String? = if (it.isCommented()) null else buildString {
        it.joinTo(this, "") { it.accept(this@CommentAdder) }
        insertMultiline(it)
    }

    private fun StringBuilder.insertMultiline(scope: Scope) {
        val range = affectedRange(scope.enclosure)
        if (range.last == lastIndex) append("*/")
        else insert(range.last + 1, "*/")
        insert(range.first, "/*")
    }

    private fun map(token: Token) = map(token.value)
    private fun map(str: CharSequence) = remap(str, '*', '^')
}