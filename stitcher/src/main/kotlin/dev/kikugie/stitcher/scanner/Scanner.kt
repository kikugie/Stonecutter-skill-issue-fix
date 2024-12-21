package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.ContentType.*
import dev.kikugie.stitcher.data.token.Token

/**
 * Determines which parts of the input string are considered to be comments.
 * Comments inside string templates will not be detected
 * due to language specific behaviour and should be avoided.
 * ```kotlin
 * "Template: ${/* not detected */variable}"
 * ```
 *
 * @property input String to scan
 * @property recognizers The collection of comment recognizers to use for tokenization
 */
class Scanner(
    private val input: CharSequence,
    private val recognizers: Iterable<CommentRecognizer>
) : Iterator<Token> {
    companion object {
        /**
         * Scans the input [CharSequence] using the provided [CommentRecognizer]s and returns an [Iterable] of Tokens.
         *
         * @param input String to scan
         * @param recognizers The collection of comment recognizers to use for tokenization
         * @return an [Iterable] of [Token]s representing the scanned input
         */
        fun scan(input: CharSequence, recognizers: Iterable<CommentRecognizer>) =
            Scanner(input, recognizers).asIterable()
    }

    /**
     * Length of the input.
     *
     * *Calling `input.length` every time makes it 9% slower, good job JVM constant evaluation!*
     */
    private val length = input.length

    /**
     * Current position in the input string.
     */
    private var cursor = 0

    /**
     * Last appended position. Used to remember where to slice the string.
     */
    private var checkpoint = 0

    /**
     * The current quote enclosure type. Comments are not detected if it's not null.
     * Updated by [quoteStatus].
     */
    private var quote: Quote? = null

    /**
     * The current comment type. Must match [CommentRecognizer.end] to exit the comment.
     */
    private var comment: CommentRecognizer? = null

    /**
     * Stores the generated tokens, depleting before [advance] can be called.
     */
    private var buffer: ArrayDeque<Token> = ArrayDeque(4)

    /**
     * @return an [Iterable] for convenience
     */
    fun asIterable(): Iterable<Token> = Iterable { this }

    override fun hasNext(): Boolean =
        buffer.isNotEmpty() || advance()

    override fun next(): Token {
        if (!hasNext()) throw NoSuchElementException()
        return buffer.removeFirst()
    }

    private fun advance(): Boolean {
        if (cursor >= length) return false
        while (true) {
            if (!advanceQuote())
                return wrapRemaining()
            val added = advanceRecognizers()
            if (cursor >= length)
                return wrapRemaining()
            if (added) return true
            cursor++
        }
    }

    private fun wrapRemaining(): Boolean {
        if (cursor > checkpoint)
            token(checkpoint, length, if (comment != null) COMMENT else CONTENT)
        buffer.add(Token.EMPTY)
        return true
    }

    private fun advanceQuote(): Boolean { // True when can process further
        while (quoteStatus()) if (++cursor >= length) {
            return false
        }
        return true
    }

    private fun advanceRecognizers(): Boolean { // True when have added to the list
        if (cursor >= length) return false
        if (comment == null) for (rec in recognizers) {
            val match = rec.start(input, cursor)
            if (match < 0) continue
            if (checkpoint != cursor)
                token(checkpoint, cursor, CONTENT)
            token(cursor, cursor + match, COMMENT_START)
            comment = rec
            cursor += match
            checkpoint = cursor
            return true
        } else {
            val match = comment!!.end(input, cursor)
            if (match < 0) return false
            token(checkpoint, cursor, COMMENT)
            token(cursor, cursor + match, COMMENT_END)
            comment = null
            cursor += match
            checkpoint = cursor
            return true
        }
        return false
    }

    /**
     * Checks if the cursor is in quoted state or should exit it.
     * Matches against non-escaped `'`, `"`, `'''`, `"""`. Quote must be closed by the same sequence that opened it
     *
     * If quote matches sets the cursor at the last character of the quote.
     * @return `true` if the cursor is inside a quoted string
     */
    private fun quoteStatus(): Boolean {
        if (comment != null || cursor >= length) return false
        when (input[cursor]) {
            '\'' -> if (getAt(cursor - 1) != '\\') when (quote) {
                Quote.SINGLE -> quote = null
                Quote.DOC_SINGLE -> if (next2Are('\'')) {
                    cursor += 2; quote = null
                }

                null -> if (next2Are('\'')) {
                    cursor += 2; quote = Quote.DOC_SINGLE
                } else quote = Quote.SINGLE

                else -> {}
            }

            '"' -> if (getAt(cursor - 1) != '\\') when (quote) {
                Quote.DOUBLE -> quote = null
                Quote.DOC_DOUBLE -> if (next2Are('"')) {
                    cursor += 2; quote = null
                }

                null -> if (next2Are('"')) {
                    cursor += 2; quote = Quote.DOC_DOUBLE
                } else quote = Quote.DOUBLE

                else -> {}
            }
        }
        return quote != null
    }

    private fun token(start: Int, end: Int, type: ContentType) {
        buffer.add(Token(input.substring(start, end), type))
    }

    private fun next2Are(char: Char): Boolean =
        getAt(cursor + 1) == char && getAt(cursor + 2) == char

    private fun getAt(index: Int, default: Char = ' ') =
        if (index >= 0 && index < length) input[index] else default

    private enum class Quote {
        SINGLE, DOUBLE, DOC_SINGLE, DOC_DOUBLE
    }
}