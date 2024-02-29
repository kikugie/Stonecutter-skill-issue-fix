package dev.kikugie.stitcher.util

import dev.kikugie.stitcher.scanner.CommentType
import dev.kikugie.stitcher.token.Token

suspend inline fun SequenceScope<Token>.yield(
    value: CharSequence,
    range: IntRange,
    type: CommentType
) = yield(Token(value.toString(), range, type))

fun IntRange.shift(other: IntRange): IntRange {
    val shift = last - first
    return other.first + first..other.first + shift + first
}

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