package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.util.shift
import dev.kikugie.stitcher.util.yield
import java.io.Reader

/**
 * Separates comments and source contents to ease further lexical analysis.
 *
 * @property input The reader object to read input from.
 * @property recognizers The collection of comment recognizers to use for tokenization.
 */
class Scanner(
    private val input: Reader,
    private val recognizers: Collection<CommentRecognizer>,
) {
    private val buffer = StringBuilder()
    private var cursor = 0
    private var current: CommentRecognizer? = null
    private var quote: Quote? = null

    fun tokenize(): Sequence<Token> = sequence {
        input.readLigatures { scan(it) }
        if (buffer.isNotEmpty()) yield(
            buffer,
            cursor - buffer.length..<cursor,
            if (current == null) Comment.CONTENT else Comment.COMMENT
        )
        yield(Token.eof(cursor))
    }

    private suspend fun SequenceScope<Token>.scan(str: String) {
        cursor++
        if (updateQuoteStatus().also { buffer.append(str) })
            return
        if (current == null) for (rec in recognizers) {
            val match = rec.start(buffer) ?: continue
            val start = cursor - buffer.length
            val range = match.range.shift(start)
            buffer.delete(match.range)
            if (buffer.isNotEmpty())
                yield(buffer, start..<range.first, Comment.CONTENT)
            yield(match.value, range, Comment.COMMENT_START)
            buffer.clear()
            current = rec
        } else {
            val match = current?.end(buffer) ?: return
            val start = cursor - buffer.length
            val range = match.range.shift(start)
            buffer.delete(match.range)
            yield(buffer, start..<range.first, Comment.COMMENT)
            yield(match.value, range, Comment.COMMENT_END)
            buffer.clear()
            current = null
        }
    }

    private fun updateQuoteStatus(): Boolean {
        if (current != null) return false
        val end = buffer.takeLast(3)
        if (quote == null) quote = Quote.match(end)
        else if (buffer.endsWith(quote!!.sequence) && !buffer.endsWith("\\${quote!!.sequence}"))
            quote = null
        return quote != null
    }

    private inline fun Reader.readLigatures(action: (String) -> Unit) {
        var char: Char
        var captureCR = false
        while (read().also { char = it.toChar() } != -1) when {
            char == '\r' -> captureCR = true
            captureCR ->
                if (char == '\n')
                    action("\r\n")
                else {
                    action("\r")
                    action(char.toString())
                }.also { captureCR = false }
            else -> action(char.toString())
        }
    }

    private fun StringBuilder.delete(range: IntRange) =
        deleteRange(range.first, range.last + 1)

    private enum class Quote(val sequence: String) {
        SINGLE("'"),
        DOUBLE("\""),
        DOC_SINGLE("'''"),
        DOC_DOUBLE("\"\"\"");

        companion object {
            fun match(chars: CharSequence) = when {
                chars.lastOrNull() == '\'' -> SINGLE
                chars.lastOrNull() == '"' -> DOUBLE
                chars == DOC_SINGLE.sequence -> DOC_SINGLE
                chars == DOC_DOUBLE.sequence -> DOC_DOUBLE
                else -> null
            }
        }
    }
}