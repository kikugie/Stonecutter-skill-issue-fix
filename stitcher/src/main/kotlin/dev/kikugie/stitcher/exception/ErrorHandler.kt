package dev.kikugie.stitcher.exception

fun ErrorHandler.accept(pos: Int, error: String) = accept(pos..<pos, error)
fun ErrorHandler.accept(range: IntRange, error: String) = accept(range, SyntaxException(error))

interface ErrorHandler {
    val errors: Iterable<Pair<IntRange, Throwable>>
    fun accept(range: IntRange, error: Throwable)
    fun addSilent(range: IntRange, error: Throwable)
}

open class ErrorHandlerImpl(private val sequence: CharSequence) : ErrorHandler {
    override val errors = mutableListOf<Pair<IntRange, Throwable>>()
    override fun accept(range: IntRange, error: Throwable) {
        errors += range to error
        println("""
            [Stitcher] ${error::class.simpleName}: ${error.message}
            $sequence
            ${" ".repeat(range.first)}${"^".repeat((1 + range.last - range.first).coerceAtLeast(1))}
        """.trimIndent())
    }

    override fun addSilent(range: IntRange, error: Throwable) {
        errors += range to error
    }
}