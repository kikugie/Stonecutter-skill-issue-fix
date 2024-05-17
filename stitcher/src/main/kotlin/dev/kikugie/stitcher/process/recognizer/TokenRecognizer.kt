package dev.kikugie.stitcher.process.recognizer

import dev.kikugie.stitcher.data.Token.Match
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

/**
 * Interface for token recognizers.
 *
 * Token recognizers are used to generify the lexical analysis and reduce boilerplate.
 *
 * @see TokenMatch
 */
interface TokenRecognizer {
    fun recognize(value: String, start: Int): Match?
}

/**
 * Matches a regular expression. Matches after the specified start, but allowed to use lookaround beyond the boundaries.
 *
 * *Why do I have this here, though?*
 *
 * @param regex The regular expression pattern as a Pattern object.
 */
class RegexRecognizer(val regex: Pattern) : TokenRecognizer {
    constructor(@Language("RegExp") regex: String) : this(Pattern.compile(regex))

    override fun recognize(value: String, start: Int): Match? {
        val matcher = regex.matcher(value).apply {
            region(start, value.length)
            useTransparentBounds(true)
            useAnchoringBounds(false)
        }
        return if (matcher.lookingAt()) matcher.group() and matcher.start()..<matcher.end()
        else null
    }
}

/**
 * Matches an exact string.
 *
 * *Why? Because faster than [RegexRecognizer], that's why!*
 *
 * @param pattern The pattern to be searched within a string.
 */
class StringRecognizer(private val pattern: String) : TokenRecognizer {
    override fun recognize(value: String, start: Int): Match? = when {
        start + pattern.length > value.length -> null // Not enough space to fit the pattern
        value.substring(start, start + pattern.length) != pattern -> null // No match
        else -> pattern and start..<start + pattern.length
    }
}

/**
 * Matches a single key character.
 *
 * *Why? Because simpler than [StringRecognizer], that's why!*
 *
 * @param char The character to be recognized.
 */
class CharRecognizer(val char: Char) : TokenRecognizer {
    override fun recognize(value: String, start: Int): Match? =
        if (value[start] == char) char.toString() and start..start else null
}

private infix fun String.and(p2: IntRange) = Match(this, p2)