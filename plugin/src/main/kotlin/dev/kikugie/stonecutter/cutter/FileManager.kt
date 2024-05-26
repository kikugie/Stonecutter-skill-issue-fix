package dev.kikugie.stonecutter.cutter

import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.FileParser
import dev.kikugie.stitcher.process.Transformer
import dev.kikugie.stitcher.process.cache.ProcessCache
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.transformer.Container
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.*

object FileManager {
    @OptIn(ExperimentalSerializationApi::class)
    fun processFile(
        file: Path,
        filter: (Path) -> Boolean,
        charset: Charset = StandardCharsets.ISO_8859_1,
        recognizers: Iterable<CommentRecognizer>,
        data: Container,
        cacheDirectory: Path,
    ): String? {
        if (!filter(file)) return null
        fun String.parser() = FileParser(reader(), recognizers)

        val text = file.readText(charset)
        val hash = text.hash("MD5")

        val cacheFile = cacheDirectory.resolve("${file.fileName.name}_$hash.ast")
        var overwrite = false
        fun createCache(): ProcessCache {
            overwrite = true
            val parser = text.parser()
            val ast = parser.parse()
            return if (parser.errs.isEmpty()) ProcessCache(data, ast)
            else {
                val message = buildString {
                    for (err in parser.errs) {
                        append(err.message)
                        append('\n')
                    }
                }
                throw SyntaxException(message)
            }
        }
        val cache: ProcessCache = try {
            val intermediate: ProcessCache = Cbor.Default.decodeFromByteArray(cacheFile.readBytes())
            if (intermediate.container == data) intermediate else createCache()
        } catch (_: Exception) {
            createCache()
        }
        if (overwrite) {
            Files.createDirectories(cacheDirectory)
            cacheDirectory.listDirectoryEntries().forEach {
                if (it.fileName.name.startsWith(file.fileName.name)) it.deleteExisting()
            }
            cacheFile.writeBytes(
                Cbor.Default.encodeToByteArray(cache.ast),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
        Transformer(cache.ast, recognizers, cache.container).process()
        val result = cache.ast.accept(Assembler)
        return if (result == text) null else result
    }


    fun String.hash(algorithm: String): String = MessageDigest.getInstance(algorithm).apply {
        this@hash.byteInputStream().use {
            val buffer = ByteArray(1024)
            var read = it.read(buffer)

            while (read != -1) {
                update(buffer, 0, read)
                read = it.read(buffer)
            }
        }
    }.digest().joinToString("") { "%02x".format(it) }
}