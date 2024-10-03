package dev.kikugie.stonecutter.process

import dev.kikugie.stonecutter.process.FileProcessor.Companion.CHUNK
import dev.kikugie.stonecutter.process.FileProcessor.Companion.HASHER
import dev.kikugie.stonecutter.process.FileProcessor.Companion.SEED
import dev.kikugie.stonecutter.process.FileProcessor.Companion.set
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

internal fun createChecksum(input: String, charset: Charset = Charsets.UTF_8): ByteArray {
    val bytes = input.toByteArray(charset)
    val result = kotlin.run {
        var chunks = input.length / CHUNK * 8
        if (input.length % CHUNK != 0) chunks += 8
        ByteArray(chunks)
    }

    var index = 0
    var cursor = 0
    while (cursor < input.length) {
        val end = (cursor + CHUNK).coerceAtMost(input.length)
        val data = HASHER.hash(bytes, cursor, end - cursor, SEED)
        result[index] = data

        cursor += CHUNK
        index += 8
    }
    return result
}

@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.toHexString(): String = Base64.encode(this)