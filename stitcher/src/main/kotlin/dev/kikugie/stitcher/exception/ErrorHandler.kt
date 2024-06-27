package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.lexer.Lexer.Slice

interface ErrorHandler {
    val errors: Iterable<Pair<Slice, String>>
    fun accept(token: Slice, message: String)
}

open class ErrorHandlerImpl : ErrorHandler {
    override val errors: MutableList<Pair<Slice, String>> = mutableListOf()

    override fun accept(token: Slice, message: String) {
        errors += token to message
    }

}