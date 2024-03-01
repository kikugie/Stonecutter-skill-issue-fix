package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.token.TokenMatch

interface CommentRecognizer {
    val start: String
    val end: String

    fun start(str: CharSequence): TokenMatch? = str.match(start)
    fun end(str: CharSequence): TokenMatch? = str.match(end)
    fun CharSequence.match(match: CharSequence) = if (endsWith(match))
        TokenMatch(match.toString(), length - match.length..<length) else null
}

private fun CharSequence.matchEOL(): TokenMatch? = when {
    endsWith("\r\n") -> TokenMatch("\r\n", length - 2..<length)
    endsWith("\r") -> TokenMatch("\r", length - 1..<length)
    endsWith("\n") -> TokenMatch("\n", length - 1..<length)
    else -> null
}

data object StandardMultiLine : CommentRecognizer {
    override val start = "/*"
    override val end = "*/"
}

data object StandardSingleLine : CommentRecognizer {
    override val start = "//"
    override val end = "\n"

    override fun end(str: CharSequence) = str.matchEOL()
}

data object HashSingleLine : CommentRecognizer {
    override val start = "#"
    override val end = "\n"

    override fun end(str: CharSequence) = str.matchEOL()
}