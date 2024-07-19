package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.component.Condition
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.MarkerType.CONDITION
import dev.kikugie.stitcher.data.token.MarkerType.SWAP
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.eval.Assembler
import dev.kikugie.stitcher.eval.ConditionChecker
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.util.affectedRange
import dev.kikugie.stitcher.util.replaceKeepIndent

/**
 * Evaluates [Definition]s and modifies the AST in-place.
 *
 * @property source Scope to modify
 * @property recognizers Comment recognizers used to reparse uncommented blocks
 * @property params Input parameters
 */
// TODO: Handle different comment styles
class Transformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val params: TransformParameters,
) : Block.Visitor<Unit> {
    private val visitor = ConditionChecker(params)
    private var previousResult: Boolean = false

    /**
     * Runs the transformer on the given scope.
     *
     * @return input scope
     */
    fun process() = source.blocks.forEach { it.accept(this) }.let { source }
    private fun withSource(source: Scope) = Transformer(source, recognizers, params)
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
        val replacement = params.swaps[def.swap!!.identifier.value]
            ?: return // TODO
        val range = contents.affectedRange(def.enclosure)
        val target = contents.substring(range)
        val new = contents.replaceRange(range, target.replaceKeepIndent(replacement))
        if (contents != new) {
            val parsed = new.parse()
            withSource(parsed).process()
            it.scope.assign(parsed.blocks)
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
            !bool -> scope.assign(listOf(ContentBlock(Token(text, ContentType.CONTENT))))
            else -> {
                val parsed = text.parse()
                withSource(parsed).process()
                it.scope.assign(parsed.blocks)
            }
        }
    }

    private fun String.parse() = FileParser(Scanner(reader(), recognizers).tokenize()).parse()

    private fun Scope.assign(new: Iterable<Block>) {
        blocks.clear()
        blocks.addAll(new)
    }
}