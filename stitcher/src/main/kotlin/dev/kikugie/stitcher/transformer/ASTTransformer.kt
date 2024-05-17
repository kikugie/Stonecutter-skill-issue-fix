package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.process.Lexer
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.Scanner
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.type.StitcherToken
import dev.kikugie.stitcher.util.affectedRange

// TODO: Handle different comment styles
class ASTTransformer(
    private val source: Scope,
    private val recognizers: Iterable<CommentRecognizer>,
    private val conditions: ConditionVisitor,
    private val swaps: SwapProcessor,
) : Block.Visitor<Unit> {
    constructor(
        source: Scope,
        recognizers: Iterable<CommentRecognizer>,
        exprs: ExpressionProcessor,
        swaps: SwapProcessor.Builder,
    ) : this(source, recognizers, ConditionVisitor(exprs), swaps.build(exprs))

    private var previousResult: Boolean = false

    fun process() {
        source.blocks.forEach { visitBlock(it) }
    }

    private fun withSource(source: Scope) = ASTTransformer(source, recognizers, conditions, swaps)

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
        val newScope = scope.replaceRange(range, swaps.get(swap.identifier))
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
        val text = (if (result) CommentRemover.accept(scope) else CommentAdder.accept(scope))
            ?: return
        if (!result) {
            scope.blocks.clear()
            scope.blocks += ContentBlock(Token(text, Comment.CONTENT))
        } else {
            val parsed = text.parse()
            withSource(parsed).process()
            block.scope.blocks = parsed.blocks
        }
    }

    private fun String.parse(): Scope = reader().scan(recognizers).lex().parse()
}