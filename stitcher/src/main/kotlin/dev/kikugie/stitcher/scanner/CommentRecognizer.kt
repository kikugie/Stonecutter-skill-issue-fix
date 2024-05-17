package dev.kikugie.stitcher.scanner

import dev.kikugie.stitcher.token.TokenMatch
import dev.kikugie.stitcher.util.matchEOL

enum class CommentType {
    SINGLE_LINE, MULTI_LINE
}

/**
 * Interface for matching source comments in [Scanner]
 *
 * Matches the start and the end separately to improve scanner performance.
 * According to (very brief) tests, using regex matching makes it 1.6-2x slower.
 */
interface CommentRecognizer {
    val type: CommentType
    val start: String
    val end: String

    fun start(str: CharSequence): TokenMatch? = str.match(start)
    fun end(str: CharSequence): TokenMatch? = str.match(end)
    fun CharSequence.match(match: CharSequence) = if (endsWith(match))
        TokenMatch(match.toString(), length - match.length..<length) else null
}

/**
 * Matches `/* ... */` comments
 */
data object StandardMultiLine : CommentRecognizer {
    override val type = CommentType.MULTI_LINE
    override val start = "/*"
    override val end = "*/"
}

/**
 * Matches `// ...` comments
 */
data object StandardSingleLine : CommentRecognizer {
    override val type = CommentType.SINGLE_LINE
    override val start = "//"
    override val end = "\n"

    override fun end(str: CharSequence) = str.matchEOL()
}

/**
 * Matches `# ...` comments
 */
data object HashSingleLine : CommentRecognizer {
    override val type = CommentType.SINGLE_LINE
    override val start = "#"
    override val end = "\n"

    override fun end(str: CharSequence) = str.matchEOL()
}