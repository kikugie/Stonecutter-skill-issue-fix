package dev.kikugie.stitcher.lexer

import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength
import dev.kikugie.stitcher.data.token.TokenType

private fun Int.ranged(length: Int) = this..<this + length
private inline fun <T> Any?.check(condition: Boolean = this != null, block: () -> T): T? =
    if (condition) block() else null

private inline infix fun <T> Boolean.then(block: () -> T): T? =
    if (this) block() else null

interface TokenRecognizer {
    val type: TokenType
    fun match(value: CharSequence, start: Int): IntRange?
}

class CharRecognizer(private val char: Char, override val type: TokenType) : TokenRecognizer {
    override fun match(value: CharSequence, start: Int): IntRange? =
        (value[start] == char) then { start.ranged(1) }
}

class StringRecognizer(private val pattern: String, override val type: TokenType) : TokenRecognizer {
    override fun match(value: CharSequence, start: Int): IntRange? = when {
        start + pattern.length > value.length -> null
        value.substring(start, start + pattern.length) != pattern -> null
        else -> start.ranged(pattern.length)
    }
}

class WhitespaceRecognizer(override val type: TokenType) : TokenRecognizer {
    override fun match(value: CharSequence, start: Int): IntRange? {
        var count = 0
        for (i in start until value.length)
            if (value[i] == ' ') ++count else break
        return (count > 0) then { start.ranged(count) }
    }
}

class IdentifierRecognizer(override val type: TokenType) : TokenRecognizer {
    override fun match(value: CharSequence, start: Int): IntRange? {
        val buffer = StringBuilder()
        for (i in start until value.length) {
            val it = value[i]
            if (allowed(it)) buffer.append(it) else break
        }
        return buffer.isNotEmpty() then { start.ranged(buffer.length) }
    }

    companion object {
        fun allowed(char: Char?) = char != null && (char == '_' || char == '-' || char.isLetterOrDigit())
    }
}

class PredicateRecognizer(override val type: TokenType) : TokenRecognizer {
    override fun match(value: CharSequence, start: Int): IntRange? {
        if (start >= value.length) return null
        val newStart = value.operatorLength(start) + start

        val switch = Switch()
        var state = State.MAIN
        var i = newStart
        while (i < value.length) {
            val char = value[i]
            val next = state.transform(char, switch)
            when {
                next != null -> state = next
                !switch.state -> return null
                else -> break
            }
            ++i
        }
        return if (newStart == i) null else start..<i
    }

    private class Switch {
        var state: Boolean = false
    }

    private enum class State {
        MAIN {
            override fun transform(char: Char, switch: Switch): State? = when (char) {
                '.' -> switch.state then {
                    switch.state = false
                    MAIN
                }

                '-' -> switch.state then { PRE_MOD }
                '+' -> switch.state then { POST_MOD }
                else -> check(char.isDigit()) {
                    switch.state = true
                    MAIN
                }
            }
        },
        PRE_MOD {
            override fun transform(char: Char, switch: Switch): State? = when {
                char == '+' -> POST_MOD
                else -> isValid(char) then { PRE_MOD }
            }
        },
        POST_MOD {
            override fun transform(char: Char, switch: Switch): State? =
                isValid(char) then { POST_MOD }
        };

        protected fun isValid(char: Char) = char.isLetterOrDigit() || char == '-' || char == '.'
        abstract fun transform(char: Char, switch: Switch): State?
    }
}