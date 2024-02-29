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
    return other.first + shift..other.first + shift + first
}
