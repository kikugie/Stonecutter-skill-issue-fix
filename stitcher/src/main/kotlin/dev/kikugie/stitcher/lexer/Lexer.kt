package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.*
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.util.StringUtil.countStart

class Lexer(
    private val input: CharSequence,
    private val handler: ErrorHandler,
): LexerAccess {
    private val tokens: List<LexSlice> by lazy(::readTokens)
    private val matcher = TokenMatcher(input)
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

    private fun readTokens() = buildList<LexSlice> {
        if (input.isEmpty()) return@buildList
        var cursor = input.countStart(' ', '\t')
        when(input.getOrNull(cursor)) {
            '?' -> {
                if (cursor > 0) add(WhitespaceType, 0..<cursor)
                add(MarkerType.CONDITION, cursor..cursor)
            }
            '$' -> {
                if (cursor > 0) add(WhitespaceType, 0..<cursor)
                add(MarkerType.SWAP, cursor..cursor)
            }
            else -> add(ContentType.CONTENT, input.indices).also { return@buildList }
        }

        cursor++
        while (cursor < input.length) {
            val previous = lastOrNull { it.type != WhitespaceType }
            val slice = matcher.match(cursor)
            if (slice.type == NullType) slice.report { "Unknown token" }
            cursor = slice.range.last + 1
            add(slice)

            // We do this check in the lexer instead of the parser to avoid
            // unnecessary token matches when we know there should be no more
            if (slice.type == SCOPE_OPEN
                || slice.type == EXPECT_WORD
                || (slice.type == IDENTIFIER && previous?.type == IDENTIFIER)
                ) {
                wrapRemaining(cursor)
                break
            }
        }
    }

    private fun MutableList<LexSlice>.wrapRemaining(start: Int) {
        val cursor = start + input.countStart(start,' ', '\t')
        if (cursor > start) add(WhitespaceType, start..<cursor)
        if (cursor < input.length) slice(NullType, cursor..<input.length).let {
            add(it)
            if (input.length - cursor < 10) it.report {
                "Unknown token"
            } else it.report {
                "Code comment not properly closed"
            }
        }
    }
}