package dev.kikugie.stitcher.parser

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.semver.VersionComparisonOperator
import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.data.token.MarkerType.CONDITION
import dev.kikugie.stitcher.data.token.MarkerType.SWAP
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.semver.VersionPredicate
import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.transformer.TransformParameters

/**
 * Parses content of an individual comment into a [Definition].
 *
 * @property lexer Assigned lexer with configured char sequence for the comment
 * @property handler Exception collector
 * @property data Parameters used by the transformer to verify identifiers or `null` if no verification is needed
 */
class CommentParser(private val lexer: Lexer, internal val handler: ErrorHandler, private val data: TransformParameters? = null) {
    val errors get() = lexer.errors.asSequence() + handler.errors
    private val currentRange get() = lookup()?.range ?: lexer[-1]!!.range
    private val currentToken get() = lookup()?.toToken() ?: Token.EMPTY

    private fun advance() = lexer.advance()
    private fun lookup(offset: Int = 0) = lexer.lookup(offset)
    private fun LexSlice.toToken() = lexer.token(this).apply {
        this[IntRange::class] = this@toToken.range
    }

    /**
     * Parses the token sequence produced by the [lexer].
     *
     * @return Parsed definition or `null` if the sequence is not a Stitcher expression
     */
    fun parse(): Definition? {
        val mode = lexer.advance()?.type ?: return null
        val extension = lookup(1)?.type == SCOPE_CLOSE
        if (extension) advance() // Skip }
        val component = when (mode) {
            CONDITION -> parseCondition()
            SWAP -> parseSwap()
            else -> return null
        }
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
        while (lookup() != null) when (advance()?.type) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> {
                if (identifier == Token.EMPTY) identifier = lookup()!!.toToken()
                else handler.accept(currentRange, "Only one swap identifier is allowed")
                if (data?.swaps?.containsKey(identifier.value) == false)
                    handler.accept(currentRange, "Could not find identifier: ${identifier.value}")
            }

            else -> handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
        }
        return Swap(identifier)
    }

    private fun parseCondition(): Condition {
        val sugar = mutableListOf<Token>()
        var expression: Component = Empty

        while (lookup() != null) when (advance()?.type) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IF, ELSE, ELIF -> sugar += lookup()!!.toToken()
            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN -> expression = matchExpression()
            else -> handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
        }
        return Condition(sugar, expression)
    }

    private fun matchExpression(checkForBoolean: Boolean = true): Component = when (lookup()?.type) {
        NEGATE -> Unary(currentToken, advance().let { matchExpression(false) })
        PREDICATE -> Assignment(Token.EMPTY, matchPredicates())
        IDENTIFIER -> {
            val id = currentToken
            when (lookup(1)?.type) {
                IDENTIFIER, PREDICATE -> Assignment(Token.EMPTY, matchPredicates())
                ASSIGN -> {
                    if (data?.dependencies?.containsKey(id.value) == false)
                        handler.accept(currentRange, "Could not find identifier: ${id.value}")
                    advance() // Skip :
                    val next = lookup(1)?.type
                    if (next == IDENTIFIER || next == PREDICATE) {
                        advance()
                        Assignment(id, matchPredicates())
                    } else {
                        handler.accept(currentRange.last + 1, "Expected to have predicates")
                        Assignment(id, emptyList())
                    }
                }

                else -> {
                    if (data?.constants?.containsKey(id.value) == false)
                        handler.accept(currentRange, "Could not find constant: ${id.value}")
                    Literal(id)
                }
            }
        }

        GROUP_OPEN -> {
            advance() // Skip (
            val group = Group(matchExpression())
            if (lookup(1)?.type == GROUP_CLOSE) advance() // Skip )
            else handler.accept(currentRange.last + 1, "Expected closing bracket")
            group
        }

        else -> {
            handler.accept(currentRange, "Unexpected token: ${currentToken.value}")
            Empty
        }
    }.let { if (checkForBoolean) it.matchBoolean() else it }

    private fun matchPredicates(): List<Token> = buildList {
        while (true) when (lookup()?.type) {
            IDENTIFIER, PREDICATE -> {
                val info = currentToken.value.asVersionPredicate()
                if (info != null) currentToken[VersionPredicate::class] = info
                add(currentToken.withType(PREDICATE))

                val next = lookup(1)?.type
                if (next == IDENTIFIER || next == PREDICATE) advance()
                else break
            }

            else -> break
        }
    }

    private fun Component.matchBoolean(): Component = when (lookup(1)?.type) {
        OR, AND -> {
            val operator = advance()!!.toToken()
            advance()
            val right = matchExpression()
            Binary(this, operator, right)
        }

        else -> this
    }

    private fun String.asVersionPredicate(): VersionPredicate? {
        val len = operatorLength()
        val op = if (len == 0) VersionComparisonOperator.EQUAL
        else try {
            VersionComparisonOperator.match(substring(0, len))
        } catch (e: IllegalArgumentException) {
            handler.accept(currentRange.first, "Invalid comparison operator")
            return null
        }
        val ver = try {
            SemanticVersionParser.parse(substring(len))
        } catch (e: VersionParsingException) {
            handler.accept(currentRange, e.message ?: "Invalid semantic version")
            return null
        }
        return VersionPredicate(op, ver)
    }
}