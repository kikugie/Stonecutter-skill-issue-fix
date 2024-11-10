package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.eval.isEmpty

private val SUPERSCRIPT_OOB = "Unable to nest at level %d - max is 9. Maybe consider refactoring your code?"
private val SUPERSCRIPT_NUMBERS = charArrayOf('⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹')
internal fun Scope.isCommented() = all { it is CommentBlock || it.isEmpty() }

internal fun StringBuilder.readSuperScript(index: Int): Int =
    SUPERSCRIPT_NUMBERS.indexOf(getOrSpace(index)).coerceAtLeast(0)

// Returns true if the length was modified
internal fun StringBuilder.removeSuperScript(index: Int): Boolean = when {
    index < 0 || index >= length -> false
    getOrSpace(index) !in SUPERSCRIPT_NUMBERS -> false
    else -> true.also { deleteAt(index) }
}

internal fun StringBuilder.writeSuperScriptBefore(index: Int, depth: Int): Boolean {
    require(depth in 0..9) { SUPERSCRIPT_OOB.format(depth) }
    val char = SUPERSCRIPT_NUMBERS[depth]
    return when {
        index == 0 -> true.also { insert(0, char)}
        this[index - 1] in SUPERSCRIPT_NUMBERS -> false.also { this[index - 1] = char }
        else -> true.also { insert(index, char)}
    }
}
internal fun StringBuilder.writeSuperScriptAfter(index: Int, depth: Int): Boolean {
    require(depth in 0..9) { SUPERSCRIPT_OOB.format(depth) }
    val char = SUPERSCRIPT_NUMBERS[depth]
    return when {
        index == lastIndex -> true.also { append(char) }
        this[index + 1] in SUPERSCRIPT_NUMBERS -> false.also { this[index + 1] = char }
        else -> true.also { insert(index + 1, char)}
    }
}

internal fun CharSequence.charMatches(index: Int, char: Char): Boolean =
    if (index < 0 || index >= length) false
    else char == get(index)

internal fun CharSequence.getOrSpace(index: Int) = getOrElse(index) { ' ' }

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