package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.eval.isEmpty

internal fun Scope.isCommented() = all { it is CommentBlock || it.isEmpty() }

internal fun remap(str: CharSequence, from: Char, to: Char) = StringBuilder(str).apply {
    var index = 0
    while (true) {
        index = indexOf(from, index)
        if (index < 0) break
        if (getOrNull(index - 1) == '/' || getOrNull(index + 1) == '/')
            set(index, to)
        index++
    }
}

internal fun CharSequence.leadingSpaces() = buildString {
    for (c in this@leadingSpaces) if (c.isWhitespace()) append(c) else break
}

internal fun CharSequence.trailingSpaces() = buildString {
    for (c in this@trailingSpaces.reversed()) if (c.isWhitespace()) append(c) else break
}.reversed()

internal fun CharSequence.affectedRange(type: ScopeType): IntRange = when (type) {
    ScopeType.CLOSED -> leadingSpaces().length..<length
    ScopeType.LINE   -> filterUntil { '\r' in it || '\n' in it }
    ScopeType.WORD   -> filterUntil { it.isBlank() }
}

internal inline fun CharSequence.filterUntil(predicate: (CharSequence) -> Boolean): IntRange {
    val buffer = StringBuilder()
    for (it in ligatures()) {
        if (buffer.isNotBlank() && predicate(it)) break
        buffer.append(it)
    }
    return buffer.leadingSpaces().length..<buffer.length - buffer.trailingSpaces().length
}

private fun CharSequence.ligatures(): Iterator<String> = object : Iterator<String> {
    private val iterator = this@ligatures.iterator()
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

    private fun read() = if (iterator.hasNext()) iterator.next().code else -1
    private fun advance() {
        char = read()
    }
}

internal fun String.replaceKeepIndent(value: String): String {
    val tabIndents = firstOrNull() == '\t'
    val minCommonIndent = lines()
        .filter(String::isNotBlank)
        .minOfOrNull { it.indentWidth() }
        ?: 0
    val prepend = if (!tabIndents) " ".repeat(minCommonIndent)
    else buildString {
        append("\t".repeat(minCommonIndent / 4))
        append(" ".repeat(minCommonIndent % 4))
    }
    return value.prependIndent(prepend)
}

internal fun CharSequence.indentWidth(): Int {
    var count = 0
    for (c in this) when (c) {
        '\t' -> count += 4
        ' ' -> count++
        else -> break
    }
    return count
}