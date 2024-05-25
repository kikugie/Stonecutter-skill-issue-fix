package dev.kikugie.stitcher.process.transformer

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.transformer.CommentAdder.onAddComment
import dev.kikugie.stitcher.util.affectedRange
import dev.kikugie.stitcher.util.leadingSpaces

const val KEY = '^'
private fun String.replaceAll(keys: Iterable<Pair<String, String>>): String {
    var str = this
    keys.forEach { (k, v) -> str = str.replace(k, v) }
    return str
}

private fun Scope.isCommented(): Boolean = !any { it !is CommentBlock && !it.isEmpty() }

object CommentRemover : Block.Visitor<String> {
    private val onRemoveComment = onAddComment.map { (k, v) -> v to k }

    private fun Block.removeComments() = accept(Assembler).replaceAll(onRemoveComment)
    override fun visitContent(it: ContentBlock) = it.removeComments()
    override fun visitComment(it: CommentBlock): String = it.removeComments()
    override fun visitCode(it: CodeBlock): String = it.removeComments()

    fun accept(type: ScopeType, scope: Scope): String? = when (type) {
        ScopeType.CLOSED -> if (!scope.isCommented()) null else scope.blocks.joinToString("") { it.accept(this) }

        else -> {
            val toUncommentIndex = scope.indexOfFirst { !it.isEmpty() }.takeIf { it >= 0 }
            if (toUncommentIndex == null) null
            else {
                val toUncomment = scope.blocks[toUncommentIndex]
                if (toUncomment !is CommentBlock) null
                else {
                    scope.blocks[toUncommentIndex] = ContentBlock(Token(toUncomment.accept(this), ContentType.COMMENT))
                    scope.accept(Assembler)
                }
            }
        }
    }
}

object CommentAdder : Block.Visitor<String> {
    internal val onAddComment = listOf(
        "/*" to "/$KEY",
        "*/" to "$KEY/",
    )

    private fun Block.addComments() = accept(Assembler).replaceAll(onAddComment)
    override fun visitContent(it: ContentBlock): String = it.addComments()
    override fun visitComment(it: CommentBlock): String = it.addComments()
    override fun visitCode(it: CodeBlock): String = it.addComments()

    fun accept(type: ScopeType, scope: Scope): String? = if (scope.isCommented()) null else buildString {
        val processed = scope.blocks.joinToString("") { it.accept(this@CommentAdder) }
        if (type == ScopeType.CLOSED) {
            val spaces = processed.leadingSpaces()
            append(processed.subSequence(0..<spaces))
            append(StandardMultiLine.start)
            append(processed.subSequence(spaces..<processed.length))
            append(StandardMultiLine.end)
        } else {
            val range = processed.affectedRange(type)
            append(processed.subSequence(0..<range.first))
            append(StandardMultiLine.start)
            append(processed.subSequence(range))
            append(StandardMultiLine.end)
            append(processed.subSequence(range.last + 1..<processed.length))
        }
    }
}