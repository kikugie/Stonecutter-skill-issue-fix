package dev.kikugie.stitcher.util

import dev.kikugie.stitcher.data.ScopeType
import java.io.Reader


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