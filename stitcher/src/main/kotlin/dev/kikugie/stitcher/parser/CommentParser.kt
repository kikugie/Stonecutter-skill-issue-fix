package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.MarkerType.*
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.WhitespaceType
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.LexerAccess
import dev.kikugie.stitcher.transformer.TransformParameters

class CommentParser(
    private val lexer: LexerAccess,
    private val handler: ErrorHandler = StoringErrorHandler(),
    private val params: TransformParameters? = null,
) {
    private val currentType get() = lexer.lookup()?.type
    private val nextType get() = lexer.lookup(1)?.type

    fun parse(): Definition? {
        val mode = lexer.lookup()?.type ?: return null
        val extension = nextType == SCOPE_CLOSE
        if (extension) consume()
        val component = when (mode) {
            CONDITION -> parseCondition()
            SWAP -> parseSwap()
            else -> return null
        }
        val closer = when (consume()?.type) {
            SCOPE_OPEN -> consume { ScopeType.CLOSED }
            EXPECT_WORD -> consume { ScopeType.WORD }
            null -> ScopeType.LINE
            else -> consume { handler.accept(it, "Invalid comment closer"); ScopeType.LINE }
        }
        if (lexer.lookup() != null)
            handler.accept(lexer.lookupOrDefault(1), "Expected end of comment")
        return Definition(component, extension, closer)
    }

    private fun parseSwap(): Swap {
        var identifier = Token.EMPTY
        while (true) when (nextType) {
            WhitespaceType -> consume()
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> consume {
                if (identifier == Token.EMPTY) identifier = it.token
                else handler.accept(it, "Duplicate identifier")
                if (params != null && it.value !in params.swaps)
                    handler.accept(it, "Unknown swap")
            }

            else -> consume { handler.accept(it, "Unexpected token") }
        }
        return Swap(identifier)
    }

    private fun parseCondition(): Condition {
        val sugar = mutableListOf<Token>()
        var expression: Component = Empty

        while (true) when (nextType) {
            WhitespaceType -> consume()
            SCOPE_OPEN, EXPECT_WORD, NullType, null -> break
            IF, ELSE, ELIF -> consume { sugar += it.token }
            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN -> expression = matchExpression()
            else -> consume { handler.accept(it, "Unexpected token") }
        }
        return Condition(sugar, expression)
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
                    handler.accept(id, "Unknown dependency")
                if (predicates.isEmpty())
                    handler.accept(it, "No predicates")
                matchBoolean(Assignment(id.token, predicates))
            } else {
                if (params != null && id.value !in params.constants)
                    handler.accept(id, "Unknown dependency")
                matchBoolean(Literal(id.token))
            }
        }
        GROUP_OPEN -> consume {
            val group = Group(matchExpression())
            if (nextType == GROUP_CLOSE) consume()
            else handler.accept(lexer.lookupOrDefault(), "Expected ')' to close the group")
            matchBoolean(group)
        }
        else -> consume {
            handler.accept(it, "Unexpected token")
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

    private fun consume(): LexSlice? {
        return lexer.advance()
    }

    private inline fun <T> consume(action: (LexSlice) -> T): T {
        return action(lexer.advance() ?: lexer.lookupOrDefault())
    }
}