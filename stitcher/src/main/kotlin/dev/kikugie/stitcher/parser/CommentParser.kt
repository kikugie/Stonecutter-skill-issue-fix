package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.*
import dev.kikugie.stitcher.data.token.MarkerType.CONDITION
import dev.kikugie.stitcher.data.token.MarkerType.SWAP
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.eval.isBlank
import dev.kikugie.stitcher.eval.isEmpty
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.LexerAccess
import dev.kikugie.stitcher.transformer.TransformParameters
import kotlin.math.min

class CommentParser(
    private val lexer: LexerAccess,
    private val handler: ErrorHandler,
    private val params: TransformParameters? = null,
) : LexerAccess by lexer {
    private val nextType get() = lookup()?.type

    fun parse(): Definition? {
        val marker = consume()?.type as? MarkerType ?: return null
        val extension = (nextType == SCOPE_CLOSE).also {
            if (it) consume()
        }

        val component = when (marker) {
            CONDITION -> parseCondition(extension)
            SWAP -> parseSwap(extension)
        }

        val closerToken = consume()
        val closer = when (closerToken?.type) {
            SCOPE_OPEN -> consume { ScopeType.CLOSED }
            EXPECT_WORD -> consume { ScopeType.WORD }
            null -> ScopeType.LINE
            else -> consume {
                it.report { "Invalid comment closer" }
                ScopeType.LINE
            }
        }
        if (closerToken != null && component.isEmpty()) closerToken.report {
            "Scope specifiers are not allowed in closer blocks"
        }
        if (nextType != null) lookup()!!.report {
            "Expected end of comment"
        }
        return Definition(component, extension, closer)
    }

    private fun parseSwap(extension: Boolean): Swap {
        var identifier = Token.EMPTY
        while (true) when (nextType) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> consume {
                if (extension) return@consume it.report {
                    "Identifiers are not allowed in swap closers"
                }
                if (identifier.isEmpty()) identifier = it.token
                else it.report { "Duplicate swap identifier" }

                if (params != null && it.value !in params.swaps)
                    handler.accept(it, "Unresolved swap identifier")
            }
            else -> consume { it.report { "Unexpected token" } }
        }
        return Swap(identifier)
    }

    private fun parseCondition(extension: Boolean): Condition {
        // Save to report after the sugar errors
        val errors = mutableListOf<LexSlice>()
        val sugar = mutableListOf<LexSlice>()
        var expression: Component = Empty

        while (true) when (nextType) {
            SCOPE_OPEN, EXPECT_WORD, NullType, null -> break
            IF, ELSE, ELIF -> consume {
                if (expression.isBlank()) sugar += it
                else errors += it
            }
            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN ->
                if (expression.isBlank()) expression = matchExpression()
                else consume { errors += it }
            else -> consume { errors += it }
        }
        validateCondition(extension, sugar, expression)
        errors.forEach { it.report { "Unexpected token" } }
        return Condition(sugar.map { it.token }, expression)
    }

    private fun matchExpression(): Component = when (nextType) {
        NEGATE -> consume { matchBoolean(Unary(it.token, matchExpression())) }
        PREDICATE -> matchBoolean(Assignment(Token.EMPTY, matchPredicates()))
        IDENTIFIER -> consume { id ->
            if (nextType == WhitespaceType) consume()
            if (nextType == ASSIGN) consume {
                val predicates = matchPredicates()
                if (params != null && id.value !in params.dependencies) it.report {
                    "Unresolved dependency"
                }
                if (predicates.isEmpty()) it.report {
                    "Missing predicates"
                }
                matchBoolean(Assignment(id.token, predicates))
            } else {
                if (params != null && id.value !in params.constants) id.report {
                    "Unresolved constant"
                }
                matchBoolean(Literal(id.token))
            }
        }
        GROUP_OPEN -> consume {
            val group = Group(matchExpression())
            if (nextType == GROUP_CLOSE) consume()
            else lexer.peek()!!.end().report {
                "Expected ')' to close the group"
            }
            matchBoolean(group)
        }
        else -> consume {
            it.report { "Unexpected token" }
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
        fun reportRest(n: Int) = sugar.drop(n).forEach { it.report { "Invalid condition sugar" } }
        when (sugar.firstOrNull()?.type) {
            IF -> {
                if (isExtension) sugar.first().report {
                    "Expected 'else' or 'elif' to follow the extension"
                }
                if (component.isEmpty()) sugar.last().end().report {
                    "Must have a condition statement"
                }
                reportRest(1)
            }
            ELIF -> {
                if (!isExtension) sugar.first().report {
                    "Expected to follow '}' to extend the condition"
                }
                if (component.isEmpty()) sugar.last().end().report {
                    "Must have a condition statement"
                }
                reportRest(1)
            }
            ELSE -> {
                if (!isExtension) sugar.first().report {
                    "Expected to follow '}' to extend the condition"
                }
                when (sugar.getOrNull(1)?.type) {
                    IF -> if (component.isEmpty()) sugar.last().end().report {
                        "Must have a condition statement"
                    }.also { reportRest(2) }
                    null -> if (component.isNotEmpty()) sugar.last().end().report {
                        "Must not have a condition statement"
                    }.also { reportRest(1) }
                    else -> reportRest(1)
                }
            }
            null -> return
            else -> reportRest(0)
        }
    }

    private fun LexSlice.end(new: TokenType = type): LexSlice =
        min(source.lastIndex, range.last + 1).let { LexSlice(new, it..it, source) }

    private fun consume(): LexSlice? = lexer.advance()
    private inline fun <T> consume(action: (LexSlice) -> T): T = action(advance()!!)
    private inline fun LexSlice.report(message: () -> String) = handler.accept(this, message())
}