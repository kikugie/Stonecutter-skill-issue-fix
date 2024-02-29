package dev.kikugie.stitcher.scanner

interface CommentRecognizer {
    val start: String
    val end: String

    fun start(str: CharSequence): TokenMatch? = str.match(start)
    fun end(str: CharSequence): TokenMatch? = str.match(end)
    fun CharSequence.match(match: CharSequence) = if (endsWith(match))
        TokenMatch(match.toString(), this.length - match.length..<this.length) else null

    data class TokenMatch(
        val token: String,
        val range: IntRange,
    )
}