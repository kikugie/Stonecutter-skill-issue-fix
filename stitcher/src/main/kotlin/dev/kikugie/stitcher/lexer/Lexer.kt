package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.MarkerType.*
import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.util.leadingSpaces
import dev.kikugie.stitcher.util.trailingSpaces

/**
 * Lexer for the Stitcher comment contents.
 *
 * @property buffer comment value
 * @property handler exception collector
 */
class Lexer(private val buffer: CharSequence, private val handler: ErrorHandler = ErrorHandlerImpl(buffer)) {
    val errors get() = handler.errors
    private val tokens = mutableListOf<LexSlice>()
    private var start = 0
    private var index = -1

    /**
     * Accesses the raw token entry from the built list.
     *
     * @param index index of the token, with negative values supported
     * @return matched buffer slice or `null` if the index is invalid
     */
    operator fun get(index: Int): LexSlice? = tokens.getOrNull(index % tokens.size)

    /**
     * Converts a slice to the associated token.
     *
     * @param slice requested buffer slice or the last one by default
     * @return token matching the slice parameters
     */
    fun token(slice: LexSlice = tokens[index]): Token = Token(buffer.substring(slice.range), slice.type)

    /**
     * Gets the token at a given offset without advancing the lexer.
     *
     * @param offset offset from the current cursor position, where negative values will get previous tokens, positive will generate tokens without advancing the cursor
     * @return slice at the requested position or `null` if no token is present there
     */
    fun lookup(offset: Int = 0): LexSlice? = when {
        offset > 0 -> tokens.getOrNull(index + offset) ?: run {
            if (start == -1) return@run null
            for (i in tokens.size..index + offset)
                advanceInternal()
            tokens.getOrNull(index + offset)
        }

        else -> tokens.getOrNull(index + offset)
    }

    /**
     * Advances the lexer cursor and returns the matched slice.
     *
     * @return the next buffer slice or `null` if no more tokens can be matched
     */
    fun advance(): LexSlice? = tokens.getOrNull(++index) ?: advanceInternal()

    private fun advanceInternal(): LexSlice? = when (start) {
        -1 -> null
        0 -> when (buffer.firstOrNull()) {
            '?' -> {
                ++start
                LexSlice(CONDITION, 0..0)
            }

            '$' -> {
                ++start
                LexSlice(SWAP, 0..0)
            }

            else -> {
                start = -1
                LexSlice(ContentType.COMMENT, buffer.indices)
            }
        }

        else -> nextToken()
    }?.also { tokens += it }

    private fun nextToken(): LexSlice? =
        locateToken(buffer, start, ALL, handler).also {
            start = if (it == null) -1
            else it.second.last + 1
        }?.let { LexSlice(it.first, it.second) }

    companion object {
        /**
         * Finds the next valid token in the provided sequence.
         * Any leading whitespaces will be ignored,
         * but unmatched values before the first match or end of the sequence will be reported.
         *
         * @param T type of the token
         * @param sequence char sequence to scan
         * @param start start offset
         * @param matchers assigned token matchers
         * @param handler exception collector
         * @return a pair of the matched type to the matched range or `null` if nothing could be matched, in which case [handler] will receive an error.
         * @see TokenRecognizer
         */
        fun <T> locateToken(
            sequence: CharSequence,
            start: Int,
            matchers: Iterable<TokenRecognizer<T>>,
            handler: ErrorHandler = ErrorHandlerImpl(sequence),
        ): Pair<T, IntRange>? {
            val buffer = StringBuilder()
            var result: Pair<T, IntRange>? = null
            outer@ for (i in start..<sequence.length) {
                for (it in matchers) {
                    val match = it.match(sequence, i) ?: continue
                    result = it.type to match.range
                    break@outer
                }
                buffer.append(sequence[i])
            }
            if (buffer.isBlank()) return result
            val range = start + buffer.leadingSpaces()..<start + buffer.length - buffer.trailingSpaces()
            handler.accept(range, "Unknown token: ${sequence.substring(range)}")
            return null
        }
    }
}