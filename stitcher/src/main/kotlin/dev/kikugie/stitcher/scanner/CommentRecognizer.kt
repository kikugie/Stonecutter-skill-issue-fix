package dev.kikugie.stitcher.scanner

/**
 * Matcher for comment start and end.
 * Used instead of regex matching because regex is very slow.
 *
 * Comments must be closed by the same recognizer that opened it.
 */
interface CommentRecognizer {
    /**
     * Detects if the comment starts at the cursor position.
     *
     * @param input Full input sequence
     * @param offset Cursor position
     * @return Length of the match or `-1` if it's not detected at the cursor
     */
    fun start(input: CharSequence, offset: Int): Int

    /**
     * Detects if the comment ends at the cursor position.
     *
     * @param input Full input sequence
     * @param offset Cursor position
     * @return Length of the match or `-1` if it's not detected at the cursor
     */
    fun end(input: CharSequence, offset: Int): Int
}

/**
 * Detects single-line comments starting with `#`.
 * *Currently unused.*
 */
data object HashCommentRecognizer : CommentRecognizer {
    override fun start(input: CharSequence, offset: Int): Int =
        if (input[offset] == '#') 1 else -1

    override fun end(input: CharSequence, offset: Int): Int =
        input.matchEOL(offset)
}

/**
 * Detects single-line comments starting with `//`.
 */
data object DoubleSlashCommentRecognizer : CommentRecognizer {
    override fun start(input: CharSequence, offset: Int): Int =
        if (input[offset] == '/' && input.getAt(offset + 1) == '/') 2 else -1

    override fun end(input: CharSequence, offset: Int): Int =
        input.matchEOL(offset)
}

/**
 * Detects `/* */` multi-line comments.
 */
data object SlashStarCommentRecognizer : CommentRecognizer {
    override fun start(input: CharSequence, offset: Int): Int =
        if (input[offset] == '/' && input.getAt(offset + 1) == '*') 2 else -1

    override fun end(input: CharSequence, offset: Int): Int =
        if (input[offset] == '*' && input.getAt(offset + 1) == '/') 2 else -1
}

object CommentRecognizers {
    val ALL = listOf(
        HashCommentRecognizer,
        DoubleSlashCommentRecognizer,
        SlashStarCommentRecognizer,
    )

    val DEFAULT = listOf(
        DoubleSlashCommentRecognizer,
        SlashStarCommentRecognizer,
    )
}

private fun CharSequence.matchEOL(offset: Int) = when (get(offset)) {
    '\r' -> if (getAt(offset + 1) == '\n') 2 else 1
    '\n' -> 1
    else -> -1
}

private fun CharSequence.getAt(index: Int, default: Char = ' ') =
    if (index >= 0 && index < length) get(index) else default