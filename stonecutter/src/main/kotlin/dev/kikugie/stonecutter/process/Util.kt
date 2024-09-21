package dev.kikugie.stonecutter.process

internal fun printErrors(vararg errors: Throwable): Unit = printErrors(0, *errors)
internal fun printErrors(indent: Int, vararg errors: Throwable): Unit = errors.forEach {
    buildString {
        appendLine("${it.message}".prependIndent('\t' * indent))
        if (it !is ProcessException) it.stackTrace.forEach { trace -> appendLine("${'\t' * (indent + 2)}at $trace") }
    }.let(::printErr)
    it.cause?.let { cause -> printErrors(indent + 1, cause) }
    it.suppressed.forEach { suppressed -> printErrors(indent + 1, suppressed) }
}

private fun printErr(any: Any) = System.err.print(any)
private operator fun Char.times(n: Int) = if (n <= 0) "" else buildString { repeat(n) { append(this@times) } }