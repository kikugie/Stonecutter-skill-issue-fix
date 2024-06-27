package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.*

class Lexer(
    private val input: CharSequence,
    private val matchers: Iterable<TokenRecognizer> = ALL,
) : LexerAccess {
    private val tokens = mutableListOf<Slice>()
    private var index = 0
    private var cursor = 0

    override fun lookupOrDefault(offset: Int): Slice = lookup(offset) ?: Slice(NullType, input.lastIndex..<input.length, input)
    override fun lookup(offset: Int) = when {
        offset >= 0 -> tokens.getOrNull(index + offset) ?: run {
            if (cursor == -1) return@run null
            for (i in tokens.size..index + offset)
                advanceInternal()
            tokens.getOrNull(index + offset)
        }

        else -> tokens.getOrNull(index + offset)
    }

    override fun advance(): Slice? = (tokens.getOrNull(index) ?: advanceInternal()).also {
        if (it == null || cursor >= input.length) cursor = -1
        if (it != null) index++
    }

    private fun advanceInternal(): Slice? = when (cursor) {
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

    private fun locateToken(): Slice? {
        if (cursor >= input.length) return null
        fun find(): Slice? = matchers.firstNotNullOfOrNull {
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

    private fun slice(type: TokenType, range: IntRange) = Slice(type, range, input)

    data class Slice(
        val type: TokenType,
        val range: IntRange,
        val source: CharSequence
    ) {
        val value get() = source.substring(range)
        val token get() = Token(value, type)

        override fun toString(): String = "Slice(type=$type, range=${range.first}..<${range.last + 1}, value=$value)"
        
        companion object {
            val EMPTY = Slice(NullType, 0..0, "")
        }
    }
}