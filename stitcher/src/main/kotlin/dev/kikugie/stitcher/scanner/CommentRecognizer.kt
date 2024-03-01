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
    endsWith("\r\n") -> TokenMatch(toString(), length - 2..<length)
    endsWith("\r") -> TokenMatch(toString(), length - 1..<length)
    endsWith("\n") -> TokenMatch(toString(), length - 1..<length)
    else -> null
}

data object StandardMultiLine : CommentRecognizer {
    override val start = "/*"
    override val end = "*/"

    override fun end(str: CharSequence) = str.matchEOL()
}

data object StandardSingleLine : CommentRecognizer {
    override val start = "//"
    override val end = "\n"
}

data object HashSingleLine : CommentRecognizer {
    override val start = "#"
    override val end = "\n"

    override fun end(str: CharSequence) = str.matchEOL()
}