package dev.kikugie.stitcher.lexer

import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength
import dev.kikugie.stitcher.data.token.Token.Match
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

/**
 * Interface for id recognizers.
 *
 * Token recognizers are used to generify the lexical analysis and reduce boilerplate.
 *
 * @see Match
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

/**
 * Matches a valid Stitcher identifier. Allowed characters are: `[a-ZA-Z0-9_-]`.
 *
 * *Why still no regex? Because no one can read regex, right?*
 */
class IdentifierRecognizer<T>(override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? {
        if (!allowed(value.getOrNull(start))) return null
        for (i in start until value.length)
            if (!allowed(value[i]))
                return value.match(start..<i)
        return value.match(start..<value.length)
    }

    companion object {
        fun allowed(char: Char?) = char != null && (char == '_' || char == '-' || char.isLetterOrDigit())
    }
}

/**
 * Matches a semi-valid semantic version predicate.
 *
 * Predicate may start with `=`, `>`, `<`, `>=`, `<=`, `~`, `^` or nothing, which will be recognized as `=` operator.
 * The version specification should match `[a-ZA-Z0-9.-+]`, which is not guaranteed to be valid semver,
 * but running the semver parser in the lexer would kill the performance, so it is done later by the parser.
 */
// TODO: Expand to be more generic
class PredicateRecognizer<T>(override val type: T) : TokenRecognizer<T> {
    override fun match(value: CharSequence, start: Int): Match? {
        val operator = value.operatorLength(start)
        var index = start + operator
        while (index < value.length) {
            if (!value[index].allowed()) break
            ++index
        }
        return if (index == start + operator) null
        else value.match(start..<index)
    }

    private fun Char.allowed() = this == '.' || this == '-' || this == '+' || isLetterOrDigit()

}

private fun CharSequence.match(range: IntRange) = Match(substring(range), range)
private infix fun String.and(p2: IntRange) = Match(this, p2)