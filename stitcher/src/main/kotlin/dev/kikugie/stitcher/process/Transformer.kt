package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.access.Constants
import dev.kikugie.stitcher.process.access.ExpressionProcessor
import dev.kikugie.stitcher.process.access.Expressions
import dev.kikugie.stitcher.process.access.Swaps
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.transformer.CommentAdder
import dev.kikugie.stitcher.process.transformer.CommentRemover
import dev.kikugie.stitcher.process.transformer.ConditionVisitor
import dev.kikugie.stitcher.type.Comment.CONTENT
import dev.kikugie.stitcher.type.StitcherToken.CONDITION
import dev.kikugie.stitcher.type.StitcherToken.SWAP
import dev.kikugie.stitcher.util.affectedRange

// TODO: Handle different comment styles
class Transformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val conditions: ConditionVisitor,
    private val swaps: Swaps,
) : Block.Visitor<Unit> {
    constructor(
        source: Scope,
        recognizers: Iterable<CommentRecognizer>,
        constants: Constants = emptyMap(),
        expressions: Expressions = emptyList(),
        swaps: Swaps = emptyMap(),
    ) : this(
        source,
        recognizers,
        ConditionVisitor(ExpressionProcessor(constants, expressions)),
        swaps
    )

    private var previousResult: Boolean = false

    fun process() {
        source.forEach { visitBlock(it) }
    }

    private fun withSource(source: Scope) = Transformer(source, recognizers, conditions, swaps)

    override fun visitContent(content: ContentBlock) {}

    override fun visitComment(comment: CommentBlock) = when (comment.scope?.type) {
        SWAP -> processSwap(comment)
        CONDITION -> processCondition(comment)
        else -> {}
    }

    private fun processSwap(block: CommentBlock) {
        val swap = block.content as Swap
        val scope = Assembler.visitScope(block.scope!!)
        val range = scope.affectedRange(block.scope.enclosure)
        val newScope = scope.replaceRange(range, swaps[swap.identifier.value]!!)
        if (scope != newScope) {
            val parsed = newScope.parse()
            withSource(parsed).process()
            block.scope.blocks = parsed.blocks
        }
    }

    private fun processCondition(block: CommentBlock) {
        val condition = block.content as Condition
        val scope = block.scope!!
        var result = condition.accept(conditions)
        if (condition.extension)
            result = !previousResult && result
        previousResult = result
        val text = (if (result) CommentRemover.accept(scope) else CommentAdder.accept(scope))
        when {
            text == null -> withSource(scope).process()
            !result -> scope.blocks = mutableListOf(ContentBlock(Token(text, CONTENT)))
            else -> {
                val parsed = text.parse()
                withSource(parsed).process()
                block.scope.blocks = parsed.blocks
            }
        }
    }

    private fun String.parse(): Scope = reader().scan(recognizers).lex().parse()

    companion object {
        fun Scope.transform(
            recognizers: Iterable<CommentRecognizer>,
            constants: Constants = emptyMap(),
            expressions: Expressions = emptyList(),
            swaps: Swaps = emptyMap(),
        ): Scope = this.also {
            Transformer(this, recognizers, constants, expressions, swaps).apply {
                process()
            }
        }
    }
}