package dev.kikugie.stonecutter.cutter

import dev.kikugie.stitcher.data.RootScope
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.Transformer
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.transformer.ConditionVisitor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
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
        conditions: ConditionVisitor,
        swaps: Map<String, String>,
        cacheDirectory: Path,
    ): String? {
        if (!filter(file)) return null
        fun String.parse() = reader().scan(recognizers).lex().parse()

        val text = file.readText(charset)
        val hash = text.hash("MD5")

        val cache = cacheDirectory.resolve("${file.fileName.name}_$hash.ast")
        var overwrite = false
        var ast: RootScope? = null
        if (cache.exists() && cache.isReadable()) try {
            ast = Cbor.Default.decodeFromByteArray(cache.readBytes())
        } catch (_: SerializationException) {
        }
        if (ast?.version != Parser.VERSION)
            ast = text.parse().also { overwrite = true }
        if (overwrite) {
            Files.createDirectories(cacheDirectory)
            cacheDirectory.listDirectoryEntries().forEach {
                if (it.fileName.name.startsWith(file.fileName.name)) it.deleteExisting()
            }
            cache.writeBytes(
                Cbor.Default.encodeToByteArray(ast),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
        Transformer(ast, recognizers, conditions, swaps).process()
        val result = ast.accept(Assembler)
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