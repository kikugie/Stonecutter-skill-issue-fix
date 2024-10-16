package dev.kikugie.stonecutter.process

import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.nanoseconds

internal class LogCollector(
    val logger: Logger,
    val path: Path,
    val debug: Boolean = false
) {
    private val messages = mutableListOf<String>()
    private val start = System.nanoTime()

    fun push(message: String) {
        messages.add(message)
    }

    fun push(message: String, error: Throwable) {
        val complete = buildString {
            appendLine("$message: [${error::class.simpleName}] (${error.message})")
            append(error.stackTraceToString())
        }
        push(complete)
    }

    fun release() {
        val duration = (System.nanoTime() - start).nanoseconds
        val joined = buildString {
            append("File: ${path.absolutePathString()} ")
            appendLine("($duration)")
            messages.joinToString("\n") { it.prependIndent("  ") }.also(::append)
        }
        if (debug) println(joined)
        else logger.debug(joined)
    }
}