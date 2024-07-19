package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.MarkerType.CONDITION
import dev.kikugie.stitcher.data.token.MarkerType.SWAP
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.TokenType
import dev.kikugie.stitcher.data.token.WhitespaceType
import dev.kikugie.stitcher.eval.isBlank
import dev.kikugie.stitcher.eval.isEmpty
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.LexerAccess
import dev.kikugie.stitcher.transformer.TransformParameters
import kotlin.math.min

class CommentParser(
    private val lexer: LexerAccess,
    private val handler: ErrorHandler = StoringErrorHandler(),
    private val params: TransformParameters? = null,
) {
    private val currentType get() = lexer.lookup()?.type
    private val nextType get() = lexer.lookup(1)?.type
    val errors get() = handler.errors

    fun parse(): Definition? {
        val mode = lexer.lookup()?.type ?: return null
        val extension = nextType == SCOPE_CLOSE
        if (extension) consume()
        val component = when (mode) {
            CONDITION -> parseCondition(extension)
            SWAP -> parseSwap(extension)
            else -> return null
        }
        val closerToken = consume()
        val closer = when (closerToken?.type) {
            SCOPE_OPEN -> consume { ScopeType.CLOSED }
            EXPECT_WORD -> consume { ScopeType.WORD }
            null -> ScopeType.LINE
            else -> consume { handler.accept(it, Messages.WRONG_CLOSER); ScopeType.LINE }
        }
        if (closerToken != null && component.isEmpty())
            handler.accept(closerToken, Messages.EMPTY_BODY)
        if (currentType == WhitespaceType)
            consume()
        if (lexer.lookup() != null)
            handler.accept(lexer.lookup()!!.start(), Messages.NO_END)
        return Definition(component, extension, closer)
    }

    private fun parseSwap(isExtension: Boolean): Swap {
        var identifier = Token.EMPTY
        while (true) when (nextType) {
            WhitespaceType -> consume()
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> consume {
                if (isExtension) return@consume handler.accept(it, Messages.CLOSER_SWAP)
                if (identifier == Token.EMPTY) identifier = it.token
                else handler.accept(it, Messages.DUPLICATE_SWAP)
                if (params != null && it.value !in params.swaps)
                    handler.accept(it, Messages.WRONG_SWAP)
            }
            else -> consume { handler.accept(it, Messages.WRONG_TOKEN) }
        }
        return Swap(identifier)
    }

    private fun parseCondition(isExtension: Boolean): Condition {
        // Save to report after the sugar errors
        val errors = mutableListOf<LexSlice>()
        val sugar = mutableListOf<LexSlice>()
        var expression: Component = Empty

        while (true) when (nextType) {
            WhitespaceType -> consume()
            SCOPE_OPEN, EXPECT_WORD, NullType, null -> break
            IF, ELSE, ELIF -> consume { sugar += it }
            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN ->
                if (expression.isBlank()) expression = matchExpression()
                else consume { errors += it }
            else -> consume { errors += it }
        }
        validateCondition(isExtension, sugar, expression)
        errors.forEach { handler.accept(it, Messages.WRONG_TOKEN) }
        return Condition(sugar.map { it.token }, expression)
    }

    private fun matchExpression(): Component = when (nextType) {
        WhitespaceType -> consume { matchExpression() }
        NEGATE -> consume { matchBoolean(Unary(it.token, matchExpression())) }
        PREDICATE -> matchBoolean(Assignment(Token.EMPTY, matchPredicates()))
        IDENTIFIER -> consume { id ->
            if (nextType == WhitespaceType) consume()
            if (nextType == ASSIGN) consume {
                val predicates = matchPredicates()
                if (params != null && id.value !in params.dependencies)
                    handler.accept(id, Messages.WRONG_DEP)
                if (predicates.isEmpty())
                    handler.accept(it, Messages.NO_PREDICATES)
                matchBoolean(Assignment(id.token, predicates))
            } else {
                if (params != null && id.value !in params.constants)
                    handler.accept(id, Messages.WRONG_CONST)
                matchBoolean(Literal(id.token))
            }
        }
        GROUP_OPEN -> consume {
            val group = Group(matchExpression())
            if (nextType == GROUP_CLOSE) consume()
            else handler.accept(lexer.lookupOrDefault().end(GROUP_CLOSE), Messages.NO_GROUP_END)
            matchBoolean(group)
        }
        else -> consume {
            handler.accept(it, Messages.WRONG_TOKEN)
            Empty
        }
    }

    private fun matchPredicates(): List<Token> = buildList {
        while (true) when (nextType) {
            WhitespaceType -> consume()
            PREDICATE -> consume { add(it.token) }
            else -> break
        }
    }

    private fun matchBoolean(left: Component): Component = when (nextType) {
        WhitespaceType -> consume { matchBoolean(left) }
        OR, AND -> consume { Binary(left, it.token, matchExpression()) }
        else -> left
    }

    private fun validateCondition(isExtension: Boolean, sugar: List<LexSlice>, component: Component) {
        fun reportRest(n: Int) = sugar.drop(n).forEach { handler.accept(it, Messages.WRONG_SUGAR) }

        when (sugar.firstOrNull()?.type) {
            IF -> {
                if (isExtension) handler.accept(sugar.first(), Messages.PLS_NO_EXTENSION)
                if (component.isEmpty()) handler.accept(sugar.last().end(), Messages.NO_CONDITION)
                reportRest(1)
            }
            ELIF -> {
                if (!isExtension) handler.accept(sugar.first(), Messages.NO_EXTENSION)
                if (component.isEmpty()) handler.accept(sugar.last().end(), Messages.NO_CONDITION)
                reportRest(1)
            }
            ELSE -> {
                if (!isExtension) handler.accept(sugar.first(), Messages.NO_EXTENSION)
                when (sugar.getOrNull(1)?.type) {
                    IF -> {
                        if (component.isEmpty()) handler.accept(sugar.last().end(), Messages.NO_CONDITION)
                        reportRest(2)
                    }
                    null -> {
                        if (component.isNotEmpty()) handler.accept(sugar.last().end(), Messages.PLS_NO_CONDITION)
                        reportRest(1)
                    }
                    else -> reportRest(1)
                }
            }
            null -> return
            else -> reportRest(0)
        }
    }

    private fun LexSlice.end(new: TokenType = type): LexSlice =
        min(source.lastIndex, range.last + 1).let { LexSlice(new, it..it, source) }

    private fun LexSlice.start(new: TokenType = type): LexSlice =
        min(source.lastIndex, range.first).let { LexSlice(new, it..it, source) }

    private fun consume(): LexSlice? {
        return lexer.advance()
    }

    private inline fun <T> consume(action: (LexSlice) -> T): T {
        return action(lexer.advance() ?: lexer.lookupOrDefault())
    }
}