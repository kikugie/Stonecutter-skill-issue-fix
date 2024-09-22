package dev.kikugie.stonecutter.process

import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.nanoseconds

/**
 * A class that collects and logs messages grouped by file.
 *
 * @property logger The logger to use for logging the messages
 * @property path The path to the file or location associated with the log collector
 * @property debug Flag indicating whether the log collector is in debug mode
 *
 * @constructor Creates a new instance of the LogCollector class with the specified logger, path, and debug mode
 */
class LogCollector(
    val logger: Logger,
    val path: Path,
    val debug: Boolean = false
) {
    private val messages = mutableListOf<String>()
    private val start = System.nanoTime();

    /**
     * Adds a message.
     *
     * @param message The message to be added.
     */
    fun push(message: String) {
        messages.add(message)
    }

    /**
     * Adds a message with an exception attached
     *
     * @param message The message to be added
     * @param error Exception to be reported
     */
    fun push(message: String, error: Throwable) {
        val complete = buildString {
            appendLine("$message: [${error::class.simpleName}] (${error.message})")
            append(error.stackTraceToString())
        }
        push(complete)
    }

    /**
     * Combines stored messages and prints them as a list under the specified [path].
     *
     * If [debug] is enabled it prints to the console directly.
     */
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