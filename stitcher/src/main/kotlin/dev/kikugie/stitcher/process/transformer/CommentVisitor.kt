package dev.kikugie.stitcher.process.transformer

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.transformer.CommentAdder.onAddComment
import dev.kikugie.stitcher.util.affectedRange

const val KEY = '^'
private fun String.replaceAll(keys: Iterable<Pair<String, String>>): String {
    var str = this
    keys.forEach { (k, v) -> str = str.replace(k, v) }
    return str
}

object CommentRemover : Block.Visitor<String> {
    val onRemoveComment = onAddComment.map { (k, v) -> v to k }

    override fun visitContent(content: ContentBlock) = Assembler.visitContent(content)
        .replaceAll(onRemoveComment)

    override fun visitComment(comment: CommentBlock): String = Assembler.visitComponent(comment.content)
        .replaceAll(onRemoveComment)

    fun accept(scope: Scope) = if (!scope.isCommented()) null else
        scope.blocks.joinToString(separator = "", transform = ::visitBlock)
}

object CommentAdder : Block.Visitor<String> {
    val onAddComment = listOf(
        "/*" to "/$KEY",
        "*/" to "$KEY/",
    )

    override fun visitContent(content: ContentBlock) = Assembler.visitContent(content)
        .replaceAll(onAddComment)

    override fun visitComment(comment: CommentBlock): String = Assembler.visitComponent(comment.content)
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