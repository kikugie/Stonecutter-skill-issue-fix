package dev.kikugie.stonecutter.cutter

import dev.kikugie.stonecutter.processor.CommentProcessor
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.bufferedReader
import kotlin.io.path.writeText

class FileCutter(
    val file: Path,
    val stonecutter: StonecutterTask
) {
    fun write(output: Path) {
        val transformed = try {
            CommentProcessor.process(file.bufferedReader(StandardCharsets.ISO_8859_1), stonecutter.processor)
        } catch (e: Exception) {
            throw RuntimeException("Failed processing file $file:\n${e.message}", e)
        }
        output.writeText(transformed, StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE)
    }
}