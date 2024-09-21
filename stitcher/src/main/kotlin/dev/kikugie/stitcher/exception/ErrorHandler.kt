package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.lexer.LexSlice

fun Pair<LexSlice, String>.join() = """
    $second:
    ${first.source}
    ${"~".repeat(first.value.length.coerceAtLeast(1)).prependIndent(" ".repeat((first.range.first).coerceAtLeast(0)))}
""".trimIndent()

inline fun ErrorHandler.accept(token: LexSlice, message: () -> String) {
    accept(token, message())
}

interface ErrorHandler {
    val errors: Collection<Pair<LexSlice, String>>
    fun accept(token: LexSlice, message: String)
}

open class StoringErrorHandler : ErrorHandler {
    override val errors = mutableListOf<Pair<LexSlice, String>>()
    override fun accept(token: LexSlice, message: String) {
        errors += token to message
    }
}