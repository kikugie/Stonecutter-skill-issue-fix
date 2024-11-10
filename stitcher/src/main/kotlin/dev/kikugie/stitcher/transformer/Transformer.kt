package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.MarkerType.*
import dev.kikugie.stitcher.eval.join
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.scanner.Scanner

class Transformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val params: TransformParameters,
    private val handler: ErrorHandler = StoringErrorHandler()
) : Block.Visitor<Unit> {
    private val checker = ConditionChecker(params)
    private var previousResult = false

    fun process() = source.apply { onEach { it.accept(this@Transformer) } }
    override fun visitContent(it: ContentBlock) {}
    override fun visitComment(it: CommentBlock) {}
    override fun visitCode(it: CodeBlock) = when (patch(it).scope?.type) {
        CONDITION -> processCondition(it)
        SWAP -> processSwap(it)
        null -> {}
    }

    private fun processCondition(it: CodeBlock) {
        requireNotNull(it.scope) // Should be resolved by patch method
        var enabled = try {
            it.def.condition!!.accept(checker)
        } catch (e: Exception) {
            it.def.toSlice().report { "Failed to evaluate condition: ${e.message}" }
            return
        }

        if (!it.def.extension) previousResult = false
        else enabled = !previousResult && enabled
        previousResult = enabled || previousResult

        val text = if (enabled) it.scope.accept(CommentRemover)
        else withSource(it.scope).process().accept(CommentAdder)
        when {
            text == null -> withSource(it.scope).process()
            !enabled -> listOf(ContentBlock(text)).assignTo(it.scope)
            else -> withSource(text.parse()).process().assignTo(it.scope)
        }
    }

    private fun processSwap(it: CodeBlock) {
        requireNotNull(it.scope) // Should be resolved by patch method
        val key = it.def.swap!!.identifier.value
        val replacement = params.swaps[key] ?: return run {
            it.def.toSlice().report { "Unable to find swap replacement" }
        }

        val contents = it.scope.join()
        val range = contents.affectedRange(it.def.enclosure)
        val target = contents.substring(range)
        val new = contents.replaceRange(range, target.replaceKeepIndent(replacement))
        if (contents != new) withSource(new.parse()).process().assignTo(it.scope)
    }

    private fun withSource(scope: Scope) = Transformer(scope, recognizers, params, handler)
    private fun String.parse() = FileParser(Scanner(this, recognizers).asIterable(), params, handler).parse()
    private fun Iterable<Block>.assignTo(scope: Scope) {
        scope.clear()
        scope.addAll(this)
    }
    private fun Definition.toSlice() = join().let { LexSlice(type!!, it.indices, it) }
    private inline fun LexSlice.report(message: () -> String) = handler.accept(this, message())
    private fun patch(it: CodeBlock): CodeBlock = it.apply {
        scope?.type = def.type
        scope?.enclosure = def.enclosure
    }
}