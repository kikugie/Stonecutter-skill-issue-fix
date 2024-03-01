package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.util.yield
import java.io.Reader

class Scanner(
    private val input: Reader,
    private val recognizers: Collection<CommentRecognizer>,
) {
    private val buffer = StringBuilder()
    private var cursor = 0
    private var current: CommentRecognizer? = null
    private var quote: Quote? = null

    fun tokenize(): Sequence<Token> = sequence {
        input.readChars { char(it) }
        if (buffer.isNotEmpty())
            yield(
                buffer,
                cursor - buffer.length..<cursor,
                if (current == null) CommentType.CONTENT else CommentType.COMMENT
            )
    }

    private suspend fun SequenceScope<Token>.char(char: Char) {
        cursor++
        if (updateQuoteStatus().also { buffer.append(char) })
            return

        if (current == null) for (rec in recognizers) {
            val match = rec.start(buffer) ?: continue
            val start = cursor - buffer.length
            val range = match.range.shift(start)
            buffer.delete(match.range)
            if (buffer.isNotEmpty())
                yield(buffer, start..<range.first, CommentType.CONTENT)
            yield(match.value, range, CommentType.COMMENT_START)
            buffer.clear()
            current = rec
        } else {
            val match = current?.end(buffer) ?: return
            val start = cursor - buffer.length
            val range = match.range.shift(start)
            buffer.delete(match.range)
            yield(buffer, start..<range.first, CommentType.COMMENT)
            yield(match.value, range, CommentType.COMMENT_END)
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

    private inline fun Reader.readChars(action: (Char) -> Unit) {
        var char: Char
        while (read().also { char = it.toChar() } != -1) action(char)
    }

    private fun IntRange.shift(value: Int): IntRange = first + value..last + value

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