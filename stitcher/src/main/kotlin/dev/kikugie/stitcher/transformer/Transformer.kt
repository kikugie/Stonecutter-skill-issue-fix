package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.eval.Assembler
import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.component.Condition
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.data.component.Swap
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.MarkerType.CONDITION
import dev.kikugie.stitcher.data.token.MarkerType.SWAP
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.eval.ConditionChecker
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.util.leadingSpaces
import dev.kikugie.stitcher.util.trailingSpaces
import java.io.Reader

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
        val replacement = params.swaps[(def.component as Swap).identifier.value]
            ?: return // TODO
        val new = contents.replaceRange(contents.affectedRange(def.enclosure), replacement)
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

    companion object {
        internal fun String.affectedRange(type: ScopeType): IntRange = when (type) {
            ScopeType.CLOSED -> indices
            ScopeType.LINE -> filterUntil { '\r' in it || '\n' in it }
            ScopeType.WORD -> filterUntil { it.isBlank() }
        }


        private inline fun String.filterUntil(predicate: (String) -> Boolean): IntRange {
            val buffer = StringBuilder()
            for (it in reader().ligatures()) {
                if (buffer.isNotBlank() && predicate(it)) break
                buffer.append(it)
            }
            return buffer.leadingSpaces()..<buffer.length - buffer.trailingSpaces()
        }

        private fun Reader.ligatures(): Iterator<String> = object : Iterator<String> {
            private var char = read()
            private var buffer: Char? = null

            override fun hasNext() = char != -1 && buffer == null

            override fun next() = when {
                buffer != null -> buffer.toString().also { buffer = null }
                char == '\r'.code -> {
                    val next = read()
                    if (next == '\n'.code) "\r\n".also { char = read() }
                    else "\r".also { buffer = next.toChar() }
                }

                else -> char.toChar().toString().also { advance() }
            }

            private fun advance() {
                char = read()
            }
        }
    }
}