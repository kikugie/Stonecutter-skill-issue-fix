package dev.kikugie.stonecutter.cutter

import dev.kikugie.stonecutter.processor.CommentProcessor
import dev.kikugie.stonecutter.processor.StonecutterSyntaxException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.bufferedReader

object FileCutter {
    /**
     * Processes the given file.
     * @return Processed content or null if no changes were made.
     */
    @Throws(StonecutterSyntaxException::class, IOException::class, IllegalArgumentException::class)
    fun process(file: Path, stonecutter: StonecutterTask): CharSequence? = try {
        val (result, modified) = CommentProcessor.process(file.bufferedReader(StandardCharsets.ISO_8859_1), stonecutter.processor)
        if (modified) result else null
    } catch (e: Exception) {
        throw RuntimeException("Failed processing file $file:\n${e.message}", e)
    }
}