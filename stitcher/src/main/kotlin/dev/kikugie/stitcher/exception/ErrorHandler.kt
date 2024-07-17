package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.lexer.LexSlice

fun Pair<LexSlice, String>.join() = """
    $second
    ${first.source}
    ${"~".repeat(first.value.length.coerceAtLeast(1)).padStart((first.range.first - 1).coerceAtLeast(0))}
""".trimIndent()

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

open class ThrowingErrorHandler : ErrorHandler {
    override val errors: Collection<Pair<LexSlice, String>> = emptyList()

    override fun accept(token: LexSlice, message: String) {
        throw SyntaxException((token to message).join())
    }
}

open class PrintingErrorHandler : StoringErrorHandler() {
    override fun accept(token: LexSlice, message: String) {
        super.accept(token, message)
        println((token to message).join())
    }
}