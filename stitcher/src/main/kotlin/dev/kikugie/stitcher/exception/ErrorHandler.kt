package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.lexer.Lexer.Slice

fun Pair<Slice, String>.join() = """
    $second
    ${first.source}
    ${"~".repeat(first.value.length.coerceAtLeast(1)).padStart((first.range.first - 1).coerceAtLeast(0))}
""".trimIndent()

interface ErrorHandler {
    val errors: Collection<Pair<Slice, String>>
    fun accept(token: Slice, message: String)
}

open class ErrorHandlerImpl : ErrorHandler {
    override val errors: MutableList<Pair<Slice, String>> = mutableListOf()

    override fun accept(token: Slice, message: String) {
        errors += token to message
    }
}