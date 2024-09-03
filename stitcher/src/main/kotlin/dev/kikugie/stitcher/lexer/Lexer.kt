package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.*
import dev.kikugie.stitcher.exception.ErrorHandler

class Lexer(
    private val input: CharSequence,
    private val matchers: Iterable<TokenRecognizer>,
    private val handler: ErrorHandler,
): LexerAccess {
    override val errors get() = handler.errors
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

    private fun readTokens() = buildList {
        when(input.firstOrNull()) {
            '?' -> add(MarkerType.CONDITION, 0..0)
            '$' -> add(MarkerType.SWAP, 0..0)
            else -> add(ContentType.CONTENT, input.indices).also { return@buildList }
        }

        var cursor = 1
        while (cursor < input.length) {
            val slice = matchers.firstNotNullOfOrNull { rec ->
                rec.match(input, cursor)?.let { slice(rec.type, it) }
            } ?: slice(NullType, cursor..<input.length).also {
                handler.accept(it, "Unknown token")
            }
            add(slice)
            cursor = slice.range.last + 1
            // TODO: When encountering a scope closer, wrap up processing
        }
    }
}