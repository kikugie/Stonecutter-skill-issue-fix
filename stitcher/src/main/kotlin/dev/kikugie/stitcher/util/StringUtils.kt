package dev.kikugie.stitcher.util
import java.io.Reader

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

private fun Reader.ligatures(): Iterator<String> = object : Iterator<String> {
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