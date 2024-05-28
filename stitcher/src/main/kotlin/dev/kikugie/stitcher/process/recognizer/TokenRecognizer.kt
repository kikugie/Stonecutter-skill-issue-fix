package dev.kikugie.stitcher.process.recognizer

import dev.kikugie.stitcher.data.Token.Match
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

/**
 * Interface for id recognizers.
 *
 * Token recognizers are used to generify the lexical analysis and reduce boilerplate.
 *
 * @see TokenMatch
 */
interface TokenRecognizer<T> {
    val type: T
    fun match(value: CharSequence, start: Int): Match?
}

/**
 * Matches a regular expression. Matches after the specified start, but allowed to use lookaround beyond the boundaries.
 *
 * *Why do I have this here, though?*
 *
 * @param regex The regular expression pattern as a Pattern object.
 */
class RegexRecognizer<T>(val regex: Pattern, override val type: T) : TokenRecognizer<T> {
    constructor(@Language("RegExp") regex: String, type: T) : this(Pattern.compile(regex), type)

    override fun match(value: CharSequence, start: Int): Match? {
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
class StringRecognizer<T>(val pattern: String, override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? = when {
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
class CharRecognizer<T>(val char: Char, override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? =
        if (value[start] == char) char.toString() and start..start else null
}

class IdentifierRecognizer<T>(override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? {
        if (value.getOrNull(start)?.allowed() != true) return null
        for (i in start until value.length)
            if (!value[i].allowed())
                return value.match(start..<i)
        return value.match(start..<value.length)
    }

    companion object {
        fun Char.allowed() = this == '_' || this == '-' || isLetterOrDigit()
    }
}

// TODO: Expand to be more generic
class PredicateRecognizer<T>(override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? {
        val operator = value.getOperatorLength(start)
        var index = start + operator
        while (index < value.length) {
            if (!value[index].allowed()) break
            ++index
        }
        return if (index == start + operator) null
        else value.match(start..<index)
    }

    private fun Char.allowed() = this == '.' || this == '-' || this == '+' || isLetterOrDigit()

    companion object {
        fun CharSequence.getOperatorLength(start: Int = 0): Int = when (this[start]) {
            '=', '~', '^' -> 1
            '>', '<' -> if (getOrNull(start + 1) == '=') 2 else 1
            else -> 0
        }
    }
}

private fun CharSequence.match(range: IntRange) = Match(substring(range), range)
private infix fun String.and(p2: IntRange) = Match(this, p2)