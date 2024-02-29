package dev.kikugie.stitcher.lexer

import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

data class TokenMatch(val value: String, val end: Int)

interface TokenRecognizer {
    fun recognize(value: String, start: Int): TokenMatch?
}

class StringRecognizer(private val pattern: String) : TokenRecognizer {
    override fun recognize(value: String, start: Int): TokenMatch? = when {
            start + pattern.length > value.length -> null // Not enough space to fit the pattern
            value.substring(start, start + pattern.length) != pattern -> null // No match
            else -> pattern and start + pattern.length
        }
}

class CharRecognizer(val char: Char) : TokenRecognizer {
    override fun recognize(value: String, start: Int): TokenMatch? =
        if (value[start] == char) char.toString() and start + 1 else null
}

class RegexRecognizer(val regex: Pattern) : TokenRecognizer {
    constructor(@Language("RegExp") regex: String) : this(Pattern.compile(regex))

    override fun recognize(value: String, start: Int): TokenMatch? {
        val matcher = regex.matcher(value).apply {
            region(start, value.length)
            useTransparentBounds(true)
            useAnchoringBounds(false)
        }
        return if (matcher.lookingAt()) matcher.group() and matcher.end()
        else null
    }
}

private infix fun String.and(p2: Int) = TokenMatch(this, p2)