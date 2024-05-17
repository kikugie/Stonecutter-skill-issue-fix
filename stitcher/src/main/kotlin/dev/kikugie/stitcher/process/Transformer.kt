package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.access.*
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.transformer.*
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.type.StitcherToken
import dev.kikugie.stitcher.util.affectedRange

// TODO: Handle different comment styles
class Transformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val conditions: ConditionVisitor,
    private val swaps: SwapProcessor,
) : Block.Visitor<Unit> {
    companion object {
        fun create(
            source: Scope,
            recognizers: Iterable<CommentRecognizer>,
            constants: Constants = emptyMap(),
            expressions: Expressions = emptyList(),
            swaps: Swaps = emptyMap(),
        ): Transformer {
            val processor = ExpressionProcessor(constants, expressions)
            return Transformer(source, recognizers, ConditionVisitor(processor), SwapProcessor(swaps, processor))
        }
    }

    private var previousResult: Boolean = false

    fun process() {
        source.forEach { visitBlock(it) }
    }

    private fun withSource(source: Scope) = Transformer(source, recognizers, conditions, swaps)

    override fun visitContent(content: ContentBlock) {}

    override fun visitComment(comment: CommentBlock) = when (comment.scope?.type) {
        StitcherToken.SWAP -> processSwap(comment)
        StitcherToken.CONDITION -> processCondition(comment)
        else -> {}
    }

    private fun processSwap(block: CommentBlock) {
        val swap = block.content as Swap
        val scope = Assembler.visitScope(block.scope!!)
        val range = scope.affectedRange(block.scope.enclosure)
        val newScope = scope.replaceRange(range, swaps.get(swap.identifier.value))
        if (scope != newScope) {
            val parsed = newScope.parse()
            withSource(parsed).process()
            block.scope.blocks = parsed.blocks
        }
    }

    private fun processCondition(block: CommentBlock) {
        val condition = block.content as Condition
        val scope = block.scope!!
        var result = conditions.visitCondition(condition)
        if (condition.extension)
            result = !previousResult && result
        previousResult = result
        val text = (if (result) CommentRemover.accept(scope) else CommentAdder.accept(scope))
        when {
            text == null -> withSource(scope).process()
            !result -> scope.blocks = mutableListOf(ContentBlock(Token(text, Comment.CONTENT)))
            else -> {
                val parsed = text.parse()
                withSource(parsed).process()
                block.scope.blocks = parsed.blocks
            }
        }
    }

    private fun String.parse(): Scope = reader().scan(recognizers).lex().parse()
}