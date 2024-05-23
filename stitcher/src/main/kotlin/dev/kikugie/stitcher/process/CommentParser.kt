package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.process.util.LexSlice
import dev.kikugie.stitcher.data.StitcherTokenType.*
import dev.kikugie.stitcher.data.MarkerType.*

class CommentParser(private val lexer: Lexer, private val handler: ErrorHandler) {
    val errors get() = lexer.errors.asSequence() + handler.errors
    private val currentRange get() = lookup()?.range ?: lexer[-1]!!.range
    private val currentToken get() = lookup()?.toToken() ?: Token.EMPTY

    private fun advance() = lexer.advance()
    private fun lookup(offset: Int = 0) = lexer.lookup(offset)
    private fun LexSlice.toToken() = lexer.token(this)

    fun parse(): Definition? {
        val mode = lexer.advance()?.type ?: return null
        val extension = lookup(1)?.type == SCOPE_CLOSE
        if (extension) advance() // Skip }
        val component = when (mode) {
            CONDITION -> parseCondition()
            SWAP -> parseSwap()
            else -> {
                handler.accept(lexer[0]!!.range.first, "Unknown mode token: $mode")
                return null
            }
        }
        if (component.isEmpty()) advance()
        val closer = when (lookup()?.type) {
            SCOPE_OPEN -> ScopeType.CLOSED
            EXPECT_WORD -> ScopeType.WORD
            null -> ScopeType.LINE
            else -> {
                handler.accept(currentRange, "Unknown comment closer: ${currentToken.value}")
                ScopeType.LINE
            }
        }
        if (advance() != null)
            handler.accept(currentRange, "Expected comment to end")
        return Definition(component, extension, closer)
    }


    private fun parseSwap(): Swap {
        var identifier = Token.EMPTY
        while (lookup(1) != null) when (advance()!!.type) {
            SCOPE_OPEN, EXPECT_WORD -> break
            IDENTIFIER -> {
                if (identifier == Token.EMPTY) identifier = lookup()!!.toToken()
                else handler.accept(currentRange, "Only one swap identifier is allowed")
            }

            else -> handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
        }
        return Swap(identifier)
    }

    private fun parseCondition(): Condition {
        val sugar = mutableListOf<Token>()
        var expression: Component = Empty

        while (lookup(1) != null) when (advance()!!.type) {
            SCOPE_OPEN, EXPECT_WORD -> break
            IF, ELSE, ELIF -> sugar += lookup()!!.toToken()
            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN -> expression = matchExpression()
            else -> handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
        }

        return Condition(sugar, expression)
    }

    private fun matchExpression(check: Boolean = true): Component = when (lookup()?.type) {
        NEGATE -> Unary(currentToken, advance().let { matchExpression(false) })
        PREDICATE -> Assignment(Token.EMPTY, matchPredicates())
        IDENTIFIER -> {
            val id = currentToken
            if (lookup(1)?.type.let { it == IDENTIFIER || it == PREDICATE })
                Assignment(Token.EMPTY, matchPredicates())
            else if (advance()?.type == ASSIGN) {
                advance() // Skip :
                Assignment(id, matchPredicates())
            } else Literal(id)
        }

        GROUP_OPEN -> {
            advance() // Skip (
            val group = Group(matchExpression())
            if (currentToken.type == GROUP_CLOSE) advance() // Skip )
            else handler.accept(currentRange.last + 1, "Expected closing bracket")
            group
        }

        else -> {
            handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
            advance()
            Empty
        }
    }.matchBoolean(check)

    private fun matchPredicates(): List<Token> = buildList {
        while (true) when (lookup()?.type) {
            IDENTIFIER, PREDICATE -> {
                add(currentToken)
                advance()
            }
            NEGATE -> {
                handler.accept(currentRange.last, "Unary operator must be before the assignment")
                advance()
            }
            else -> break
        }
    }.also { if (it.isEmpty()) handler.accept(currentRange.last, "No predicates") }

    private fun Component.matchBoolean(check: Boolean): Component = if (!check) this else when (lookup()?.type) {
        OR, AND -> {
            val operator = currentToken
            advance()
            val right = matchExpression()
            Binary(this, operator, right)
        }

        else -> this
    }
}