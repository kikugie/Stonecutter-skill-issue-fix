package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.data.MarkerType.CONDITION
import dev.kikugie.stitcher.data.MarkerType.SWAP
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.transformer.CommentAdder
import dev.kikugie.stitcher.process.transformer.CommentRemover
import dev.kikugie.stitcher.process.transformer.ConditionVisitor
import dev.kikugie.stitcher.util.affectedRange

// TODO: Handle different comment styles
class Transformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val processor: TransformParameters,
) : Block.Visitor<Unit> {
    private val visitor = ConditionVisitor(processor)
    private var previousResult: Boolean = false

    fun process() = source.forEach { it.accept(this) }
    private fun withSource(source: Scope) = Transformer(source, recognizers, processor)
    override fun visitContent(it: ContentBlock) {}
    override fun visitComment(it: CommentBlock) {}
    override fun visitCode(it: CodeBlock) = when (it.def.type) {
        CONDITION -> processCondition(it)
        SWAP -> processSwap(it)
        else -> {}
    }

    private fun processSwap(it: CodeBlock) {
        val def = it.def
        val contents = it.scope?.accept(Assembler)
            ?: return
        val replacement = processor.swaps[(def.component as Swap).identifier.value]
            ?: return // TODO
        val new = contents.replaceRange(contents.affectedRange(def.enclosure), replacement)
        if (contents != new) {
            val parsed = new.parse()
            withSource(parsed).process()
            it.scope.blocks = parsed.blocks
        }
    }

    private fun processCondition(it: CodeBlock) {
        val def = it.def
        val scope = it.scope ?: return
        val condition = def.component as Condition
        var bool = condition.accept(visitor)
        if (!def.extension) previousResult = false
        else bool = !previousResult && bool
        previousResult = bool || previousResult

        val text = if (bool) CommentRemover.accept(def.enclosure, scope)
        else CommentAdder.accept(def.enclosure, scope)
        when {
            text == null -> withSource(scope).process()
            !bool -> scope.blocks = mutableListOf(ContentBlock(Token(text, ContentType.CONTENT)))
            else -> {
                val parsed = text.parse()
                withSource(parsed).process()
                scope.blocks = parsed.blocks
            }
        }
    }

    private fun String.parse() = FileParser(reader(), recognizers).parse()
}