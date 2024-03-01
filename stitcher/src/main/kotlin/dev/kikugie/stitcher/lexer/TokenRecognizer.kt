package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.token.TokenMatch
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

interface TokenRecognizer {
    fun recognize(value: String, start: Int): TokenMatch?
}

class StringRecognizer(private val pattern: String) : TokenRecognizer {
    override fun recognize(value: String, start: Int): TokenMatch? = when {
        start + pattern.length > value.length -> null // Not enough space to fit the pattern
        value.substring(start, start + pattern.length) != pattern -> null // No match
        else -> pattern and start..<start + pattern.length
    }
}

class CharRecognizer(val char: Char) : TokenRecognizer {
    override fun recognize(value: String, start: Int): TokenMatch? =
        if (value[start] == char) char.toString() and start..start else null
}

class RegexRecognizer(val regex: Pattern) : TokenRecognizer {
    constructor(@Language("RegExp") regex: String) : this(Pattern.compile(regex))

    override fun recognize(value: String, start: Int): TokenMatch? {
        val matcher = regex.matcher(value).apply {
            region(start, value.length)
            useTransparentBounds(true)
            useAnchoringBounds(false)
        }
        return if (matcher.lookingAt()) matcher.group() and matcher.start()..<matcher.end()
        else null
    }
}

private infix fun String.and(p2: IntRange) = TokenMatch(this, p2)