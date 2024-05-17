package dev.kikugie.stitcher.util

import dev.kikugie.stitcher.parser.ScopeType
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.token.TokenMatch
import java.io.Reader

suspend inline fun SequenceScope<Token>.yield(
    value: CharSequence,
    type: Comment,
) = yield(Token(value.toString(), type))

fun IntRange.shift(other: IntRange): IntRange {
    val shift = last - first
    return other.first + first..other.first + shift + first
}

fun IntRange.shift(value: Int): IntRange =
    first + value..last + value

fun <T : CharSequence> T.leadingSpaces(): Int {
    var spaces = 0
    for (char in this)
        if (char.isWhitespace()) spaces++ else break
    return spaces
}

fun <T : CharSequence> T.trailingSpaces(): Int {
    var spaces = 0
    for (char in this.reversed())
        if (char.isWhitespace()) spaces++ else break
    return spaces
}

inline fun Reader.readLigatures(action: (String) -> Unit) {
    var char: Char
    var captureCR = false
    while (read().also { char = it.toChar() } != -1) when {
        char == '\r' -> captureCR = true
        captureCR ->
            if (char == '\n')
                action("\r\n")
            else {
                action("\r")
                action(char.toString())
            }.also { captureCR = false }

        else -> action(char.toString())
    }
}

fun Reader.ligatures(): Iterator<String> = object : Iterator<String> {
    private var char = read()
    private var buffer: Char? = null

    override fun hasNext() = char != -1 && buffer == null

    override fun next() = when {
        buffer != null -> buffer.toString().also { buffer = null }
        char == '\r'.code -> {
            val next = read()
            if (next == '\n'.code) "\r\n".also { char = read() }
            else "\r".also { buffer = next.toChar() }
        }

        else -> char.toChar().toString().also { advance() }
    }
    private fun advance() {
        char = read()
    }
}

fun CharSequence.matchEOL(): TokenMatch? = when {
    endsWith("\r\n") -> TokenMatch("\r\n", length - 2..<length)
    endsWith("\r") -> TokenMatch("\r", length - 1..<length)
    endsWith("\n") -> TokenMatch("\n", length - 1..<length)
    else -> null
}

fun String.affectedRange(type: ScopeType): IntRange = when (type) {
    ScopeType.CLOSED -> indices
    ScopeType.LINE -> filterUntil { '\r' in it || '\n' in it }
    ScopeType.WORD -> filterUntil { it.isBlank() }
}

inline fun String.filterUntil(predicate: (String) -> Boolean): IntRange {
    val buffer = StringBuilder()
    for (it in reader().ligatures()) {
        if (buffer.isNotBlank() && predicate(it)) break
        buffer.append(it)
    }
    return buffer.leadingSpaces()..<buffer.length - buffer.trailingSpaces()
}