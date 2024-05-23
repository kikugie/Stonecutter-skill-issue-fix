package dev.kikugie.stitcher.process.util

import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.process.recognizer.TokenRecognizer
import dev.kikugie.stitcher.util.leadingSpaces
import dev.kikugie.stitcher.util.trailingSpaces

fun <T> locateToken(
    sequence: CharSequence,
    start: Int,
    matchers: Iterable<TokenRecognizer<T>>,
    handler: ErrorHandler,
): Pair<T, IntRange>? {
    val buffer = StringBuilder()
    var result: Pair<T, IntRange>? = null
    outer@ for (i in start..<sequence.length) {
        for (it in matchers) {
            val match = it.match(sequence, i) ?: continue
            result = it.type to match.range
            break@outer
        }
        buffer.append(sequence[i])
    }
    if (buffer.isBlank()) return result
    val range = start + buffer.leadingSpaces()..<start + buffer.length - buffer.trailingSpaces()
    handler.accept(range, "Unknown token: ${sequence.substring(range)}")
    return null
}