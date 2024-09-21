package dev.kikugie.stonecutter.process

import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class LogCollector(private val logger: Logger, private val path: Path) {
    private val messages = mutableListOf<String>()

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
        val joined = buildString {
            appendLine("File: ${path.absolutePathString()}")
            messages.joinToString("\n") { it.prependIndent("  ") }.also(::append)
        }
        logger.debug(joined)
    }
}