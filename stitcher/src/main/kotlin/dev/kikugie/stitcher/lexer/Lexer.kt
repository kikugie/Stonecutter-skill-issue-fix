package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.*
import dev.kikugie.stitcher.exception.ErrorHandler

class Lexer(
    private val input: CharSequence,
    private val matchers: Iterable<TokenRecognizer>,
    private val handler: ErrorHandler,
): LexerAccess {
    val tokens: List<LexSlice> by lazy(::readTokens)
    private var index: Int = -1

    override fun get(index: Int): LexSlice? = tokens.getOrNull(index)
    override fun peek(): LexSlice? = tokens.getOrNull(index)
    override fun rawLookup(offset: Int): LexSlice? = tokens.getOrNull(index + offset)
    override fun rawAdvance(): LexSlice? = tokens.getOrNull(++index)

    override fun lookup(): LexSlice? {
        var cursor = index
        do cursor++ while (tokens.getOrNull(cursor)?.type == WhitespaceType)
        return tokens.getOrNull(cursor)
    }

    override fun advance(): LexSlice? {
        do index++ while (tokens.getOrNull(index)?.type == WhitespaceType)
        return tokens.getOrNull(index)
    }

    private fun MutableList<LexSlice>.add(type: TokenType, range: IntRange) = add(slice(type, range))
    private fun slice(type: TokenType, range: IntRange) = LexSlice(type, range, input)
    private inline fun LexSlice.report(message: () -> String) = handler.accept(this, message())

    private fun countWhitespaces(start: Int): Int {
        var counter = 0
        while (input.getOrNull(start + counter)?.isWhitespace() == true) counter++
        return counter
    }

    private fun readTokens() = buildList {
        if (input.isEmpty()) return@buildList

        var cursor = countWhitespaces(0)
        when(input.getOrNull(cursor)) {
            '?' -> {
                add(WhitespaceType, 0..<cursor)
                add(MarkerType.CONDITION, cursor..cursor)
            }
            '$' -> {
                add(WhitespaceType, 0..<cursor)
                add(MarkerType.SWAP, cursor..cursor)
            }
            else -> add(ContentType.CONTENT, input.indices).also { return@buildList }
        }

        cursor++
        while (cursor < input.length) {
            val slice = matchers.firstNotNullOfOrNull { rec ->
                rec.match(input, cursor)?.let { slice(rec.type, it) }
            } ?: slice(NullType, cursor..<input.length).also {
                it.report { "Unknown token" }
            }
            add(slice)
            cursor = slice.range.last + 1

            // We do this check in the lexer instead of the parser to avoid
            // unnecessary token matches when we know there should be no more
            if (slice.type == StitcherTokenType.SCOPE_OPEN || slice.type == StitcherTokenType.EXPECT_WORD) {
                wrapRemaining(cursor)
                break
            }
        }
    }

    private fun MutableList<LexSlice>.wrapRemaining(cursor: Int) {
        var cursor1 = cursor
        val whitespaces = countWhitespaces(cursor1)
        if (whitespaces > 0) {
            add(WhitespaceType, cursor1..<cursor1 + whitespaces)
            cursor1 += whitespaces
        }

        if (cursor1 < input.length) slice(NullType, cursor1..<input.length).let {
            add(it)
            if (input.length - cursor1 < 10) it.report {
                "Unknown token"
            } else it.report {
                "Code comment not properly closed"
            }
        }
    }
}