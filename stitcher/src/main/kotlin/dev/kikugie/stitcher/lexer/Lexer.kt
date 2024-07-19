package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.*
import kotlin.math.min

class Lexer(
    private val input: CharSequence,
    private val matchers: Iterable<TokenRecognizer> = ALL,
) : LexerAccess {
    private val tokens = mutableListOf<LexSlice>()
    private var index = 0
    private var cursor = 0

    override fun lookupOrDefault(offset: Int): LexSlice = lookup(offset) ?: LexSlice(NullType, input.lastIndex..<input.length, input)
    override fun lookup(offset: Int) = when {
        offset >= 0 -> tokens.getOrNull(index + offset) ?: run {
            if (cursor == -1) return@run null
            for (i in tokens.size..index + offset)
                advanceInternal()
            tokens.getOrNull(index + offset)
        }

        else -> tokens.getOrNull(index + offset)
    }

    override fun advance(): LexSlice? = (tokens.getOrNull(index + 1) ?: advanceInternal()).also {
        if (it == null || cursor >= input.length) cursor = -1
        index = min(index + 1, tokens.size)
    }

    private fun advanceInternal(): LexSlice? = when (cursor) {
        -1 -> null
        0 -> when (input.firstOrNull()) {
            '?' -> {
                ++cursor
                slice(MarkerType.CONDITION, 0..0)
            }

            '$' -> {
                ++cursor
                slice(MarkerType.SWAP, 0..0)
            }

            else -> {
                cursor = -1
                slice(ContentType.COMMENT, input.indices)
            }
        }.also {
            tokens += it
        }

        else -> locateToken()
    }

    private fun locateToken(): LexSlice? {
        if (cursor >= input.length) return null
        fun find(): LexSlice? = matchers.firstNotNullOfOrNull {
            val match = it.match(input, cursor) ?: return@firstNotNullOfOrNull null
            val slice = slice(it.type, match)
            cursor = slice.range.last + 1
            slice
        }

        val start = cursor
        var pos = cursor
        val buffer = StringBuilder()
        while (cursor < input.length) {
            val slice = find()
            if (slice == null) buffer.append(input[cursor++])
            else return if (buffer.isEmpty()) {
                tokens += slice
                slice
            } else {
                val unknown = slice(NullType, start until pos)
                tokens += unknown
                tokens += slice
                unknown
            }
            pos++
        }
        if (buffer.isEmpty()) return null
        val unknown = slice(NullType, start until cursor)
        tokens += unknown
        return unknown
    }

    internal fun slice(type: TokenType, range: IntRange) = LexSlice(type, range, input)
    internal fun tokens() = tokens
}