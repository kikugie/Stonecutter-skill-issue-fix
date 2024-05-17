package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.assembler.AssemblyVisitor
import dev.kikugie.stitcher.parser.*
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.util.affectedRange

const val KEY = '^'
private val onAddComment = listOf(
    "/*" to "/$KEY",
    "*/" to "$KEY/",
)
private val onRemoveComment = onAddComment.map { (k, v) -> v to k }
private fun String.replaceAll(keys: Iterable<Pair<String, String>>): String {
    var str = this
    keys.forEach { (k, v) -> str = str.replace(k, v) }
    return str
}

object CommentRemover : Block.Visitor<String>, (Scope) -> String? {
    override fun visitContent(content: ContentBlock) = AssemblyVisitor.visitContent(content)
        .replaceAll(onRemoveComment)

    override fun visitComment(comment: CommentBlock): String = AssemblyVisitor.visitComponent(comment.content)
        .replaceAll(onRemoveComment)

    override fun invoke(scope: Scope) = if (!scope.isCommented()) null else
        scope.blocks.joinToString(transform = ::visitBlock)
}

object CommentAdder : Block.Visitor<String>, (Scope) -> String? {
    override fun visitContent(content: ContentBlock) = AssemblyVisitor.visitContent(content)
        .replaceAll(onAddComment)

    override fun visitComment(comment: CommentBlock): String = AssemblyVisitor.visitComponent(comment.content)
        .replaceAll(onAddComment)

    override fun invoke(scope: Scope): String? = if (scope.isCommented()) null else buildString {
        val processed = scope.blocks.joinToString(transform = ::visitBlock)
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