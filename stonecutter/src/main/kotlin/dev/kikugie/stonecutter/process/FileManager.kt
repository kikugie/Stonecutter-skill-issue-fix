package dev.kikugie.stonecutter.process

import com.charleskorn.kaml.Yaml
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitcher.eval.Assembler
import dev.kikugie.stitcher.exception.join
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stitcher.transformer.Transformer
import dev.kikugie.stitcher.scanner.CommentRecognizer
import kotlinx.serialization.*
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.*

internal class FileManager(
    private val inputCache: Path,
    private val outputCache: Path,
    private val filter: FileFilter,
    private val charset: Charset = StandardCharsets.UTF_8,
    private val recognizers: Iterable<CommentRecognizer>,
    private val params: TransformParameters,
    private val debug: Boolean,
    private val statistics: Statistics
) {
    private val parametersMatch = updateParameters() && !debug

    fun process(root: Path, source: Path): String? {
        if (!filter.shouldProcess(source)) return null
        val text = root.resolve(source).readText()
        val hash = text.hash("MD5")

        val cachePath = source.hashName(hash, source.extension)
        val cachedOutput = if (parametersMatch) getCachedOutput(cachePath) else null
        if (cachedOutput != null)
            return if (cachedOutput == text) null
            else cachedOutput

        val astPath = source.hashName(hash, "ast")
        var ast = if (parametersMatch) getCachedAst(astPath) else null
        val overwrite = ast == null
        if (ast == null) {
            val parser = FileParser(text.reader(), recognizers, params)
            ast = parser.parse()
            if (debug) cleanUpAndWrite(source, inputCache.resolve("debugAst").resolve(source.hashName(hash, "yml"))) {
                writeConfigured(Yaml.default.encodeToString(ast))
            }
            if (parser.hasErrors) throw SyntaxException(
                parser.errors.joinToString("\n\n") { it.join() }
            )
            statistics.parsed += 1
        }
        if (overwrite) cleanUpAndWrite(source, inputCache.resolve("ast").resolve(astPath)) {
            encode(ast)
        }
        Transformer(ast, recognizers, params).process()
        val result = ast.accept(Assembler)
        cleanUpAndWrite(source, outputCache.resolve("result").resolve(cachePath)) {
            writeConfigured(result)
        }
        return if (result == text) null else result
    }

    private inline fun cleanUpAndWrite(source: Path, dest: Path, action: Path.() -> Unit) = runIgnoring {
        dest.parent?.createDirectories()
        dest.cleanMatching(source.fileName.name)
        action(dest)
    }

    private fun updateParameters(): Boolean {
        val dest = outputCache.resolve("transform_parameters.yml")
        val saved: TransformParameters? = runIgnoring {
            Yaml.default.decodeFromString(dest.readText(charset))
        }
        if (saved != null && saved == params) {
            LOGGER.debug("Found matching parameters for {}", params)
            return true
        }
        runIgnoring {
            dest.parent.createDirectories()
            dest.writeConfigured(Yaml.default.encodeToString(params))
        }
        return false
    }

    private fun Path.writeConfigured(string: String) {
        writeText(string, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
    private fun Path.cleanMatching(start: String) = parent.listDirectoryEntries().forEach {
        if (it.fileName.name.startsWith(start)) it.deleteExisting()
    }
    private fun Path.hashName(hash: String, ext: String) = parent.resolve("${fileName.name}_$hash.$ext")

    private fun getCachedOutput(source: Path): String? = runIgnoring {
        val res = outputCache.resolve("result").resolve(source).readText(charset)
        LOGGER.debug("Restored {} from output cache", source)
        res
    }

    private fun getCachedAst(source: Path): Scope? = runIgnoring {
        val res: Scope = inputCache.resolve("ast").resolve(source).decode()
        LOGGER.debug("Read cached AST from {}", source)
        res
    }

    private fun String.hash(algorithm: String): String = MessageDigest.getInstance(algorithm).apply {
        this@hash.byteInputStream().use {
            val buffer = ByteArray(1024)
            var read = it.read(buffer)

            while (read != -1) {
                update(buffer, 0, read)
                read = it.read(buffer)
            }
        }
    }.digest().joinToString("") { "%02x".format(it) }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("Stonecutter")
    }
}