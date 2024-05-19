package dev.kikugie.stitcher.process.transformer

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.transformer.CommentAdder.onAddComment
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.util.affectedRange

const val KEY = '^'
private fun String.replaceAll(keys: Iterable<Pair<String, String>>): String {
    var str = this
    keys.forEach { (k, v) -> str = str.replace(k, v) }
    return str
}

private fun Scope.isCommented(): Boolean = !any { it !is CommentBlock && !it.isEmpty() }

object CommentRemover : Block.Visitor<String> {
    val onRemoveComment = onAddComment.map { (k, v) -> v to k }

    override fun visitContent(content: ContentBlock) = content.accept(Assembler)
        .replaceAll(onRemoveComment)

    override fun visitComment(comment: CommentBlock): String = comment.content.accept(Assembler)
        .replaceAll(onRemoveComment)

    fun accept(scope: Scope): String? {
        return when(scope.enclosure) {
            ScopeType.CLOSED -> if (!scope.isCommented()) null else scope.blocks.joinToString(
                separator = "",
                transform = ::visitBlock
            )

            else -> {
                val toUncommentIndex = scope.indexOfFirst { !it.isEmpty() }.takeIf { it >= 0 } ?: return null
                val toUncomment = scope.blocks[toUncommentIndex]
                if (toUncomment !is CommentBlock) return null
                scope.blocks[toUncommentIndex] = ContentBlock(Token(toUncomment.accept(this), Comment.CONTENT))
                scope.accept(Assembler)
            }
        }
    }
}

object CommentAdder : Block.Visitor<String> {
    val onAddComment = listOf(
        "/*" to "/$KEY",
        "*/" to "$KEY/",
    )

    override fun visitContent(content: ContentBlock) = content.accept(Assembler)
        .replaceAll(onAddComment)

    override fun visitComment(comment: CommentBlock): String = comment.accept(Assembler)
        .replaceAll(onAddComment)

    fun accept(scope: Scope): String? = if (scope.isCommented()) null else buildString {
        val processed = scope.blocks.joinToString(separator = "", transform = ::visitBlock)
        if (scope.enclosure == ScopeType.CLOSED)
            return "${StandardMultiLine.start}$processed${StandardMultiLine.end}"
        else {
            val range = processed.affectedRange(scope.enclosure)
            append(processed.subSequence(0..<range.first))
            append(StandardMultiLine.start)
            append(processed.subSequence(range))
            append(StandardMultiLine.end)
            append(processed.subSequence(range.last + 1..<processed.length))
        }
    }
}