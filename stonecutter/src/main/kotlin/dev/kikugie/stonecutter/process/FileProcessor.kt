package dev.kikugie.stonecutter.process

import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.eval.join
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.exception.join
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stitcher.transformer.Transformer
import dev.kikugie.stonecutter.data.YAML
import dev.kikugie.stonecutter.isAvailable
import dev.kikugie.stonecutter.useCatching
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import net.jpountz.xxhash.XXHash64
import net.jpountz.xxhash.XXHashFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.*

@OptIn(ExperimentalSerializationApi::class, ExperimentalPathApi::class)
internal class FileProcessor(
    private val dirs: DirectoryData,
    private val filter: FileFilter,
    private val charset: Charset = StandardCharsets.UTF_8,
    private val recognizers: Iterable<CommentRecognizer>,
    private val params: TransformParameters,
    private val statistics: ProcessStatistics,
    private val debug: Boolean,
) {
    private val inPlace = dirs.root == dirs.dest
    private val parametersMatch = updateParameters() && !debug

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(FileProcessor::class.java)
        val HASHER: XXHash64 = XXHashFactory.fastestInstance().hash64()
        const val CHUNK = 1024 * 16
        const val SEED: Long = -0x701074A728960DCB

        private fun Path.resolveChecked(subpath: Path) = resolve(subpath).createParentDirectories()

        private fun Path.createParentDirectories() = also {
            parent.createDirectories()
        }

        private inline fun Path.writeConfigured(block: (OutputStream) -> Unit) = outputStream(
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ).useCatching { block(it) }

        private fun Path.withExtension(ext: String) = parent.resolve("${fileName.nameWithoutExtension}.$ext")

        private fun Long.toByteList(dest: MutableList<Byte>) {
            (0 until 8).forEach { i ->
                dest.add((this shr (i * 8) and 0xFF).toByte())
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    inner class EntryProcessor(private val source: Path) {
        internal val collector = LogCollector(LOGGER, dirs.root.resolve(source), debug)

        internal fun process(): String? {
            statistics.total.getAndIncrement()
            if (!filter.shouldProcess(source)) return null logging "Skipping, filtered"

            val text = dirs.root.resolve(source).readText(charset)
            val checksum = createChecksum(text)
            val checksumsMatch = readAndCheckSums(checksum)
            if (parametersMatch && checksumsMatch && !debug) readCachedOutput()?.also {
                return if (it == text) null logging "Skipping, matches cache"
                else it logging "Found output cache"
            }

            var ast: Scope? = null
            if (checksumsMatch && !debug) ast = readCachedAst()
            val handler = StoringErrorHandler()
            if (ast == null) {
                statistics.parsed.getAndIncrement()
                val parser = FileParser.create(text, handler, recognizers, params)
                ast = parser.parse()
                collector.push("Parsed AST, ${handler.errors.size} errors")
                handler.throwIfHasErrors()
                writeCachedAst(ast)
                if (debug) writeDebugAst(ast)
            }
            Transformer(ast, recognizers, params, handler).process()
            collector.push("Transformed AST, ${handler.errors.size} errors")
            handler.throwIfHasErrors()
            val result = ast.join()
            writeCachedOutput(result)
            writeChecksum(checksum)
            return if (result == text) null logging "Skipping, matches input"
            else result logging "Successfully processed"
        }

        private fun writeDebugAst(ast: Scope) = dirs.debug
            .resolveChecked(source.withExtension("yml"))
            .runReporting("Failed to save debug AST") {
                writeConfigured { YAML.encodeToStream(ast, it) }
            }


        private fun writeCachedAst(ast: Scope) = dirs.asts
            .resolveChecked(source.withExtension("ast"))
            .runReporting("Failed to save cached AST") {
                writeConfigured { it.write(Cbor.Default.encodeToByteArray(ast)) }
            }

        private fun writeCachedOutput(result: String) = dirs.results.resolveChecked(source)
            .runReporting("Failed to save cached output") {
                writeText(result, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }

        private fun writeChecksum(checksum: ByteArray) = dirs.inputCache.resolve("checksums")
            .resolveChecked(source.withExtension("checksum"))
            .runReporting("Failed to save checksum") {
                writeBytes(checksum, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }

        private fun readCachedAst(): Scope? {
            val file = dirs.asts.resolve(source.withExtension("ast"))
            return if (!file.isAvailable()) null logging "AST cache not found"
            else file.runReporting("Failed to read cached AST") {
                Cbor.Default.decodeFromByteArray<Scope>(file.readBytes())
            }
        }

        private fun readCachedOutput(): String? {
            val file = dirs.results.resolve(source)
            return if (!file.isAvailable()) null logging "Output cache not found"
            else file.runReporting("Failed to read cached output") {
                file.readText(charset)
            }
        }

        private fun readAndCheckSums(current: ByteArray): Boolean {
            val file = dirs.inputCache.resolve("checksums").resolve(source.withExtension("checksum"))
            if (!file.isAvailable()) return false logging "Checksum file not found"

            val checksum = file.runReporting("Failed to read checksum") {
                file.readBytes()
            }

            val match = current.contentEquals(checksum)
            if (!match) null logging buildString {
                appendLine("Checksum mismatch:")
                appendLine("  Expected - ${Base64.encode(current)}")
                append("  Received - ${checksum?.let(Base64::encode)}")
            }
            return match
        }

        private fun createChecksum(input: String): ByteArray {
            val list = mutableListOf<Byte>()
            var offset = 0
            while (offset < input.length) {
                val bytes = input.substring(offset, (offset + CHUNK).coerceAtMost(input.length)).toByteArray(charset)
                val data = HASHER.hash(bytes, 0, bytes.size, SEED)
                data.toByteList(list)
                offset += CHUNK
            }
            return list.toByteArray()
        }

        private inline fun <T, R> T.runReporting(message: String, action: T.() -> R): R? = kotlin.runCatching {
            action()
        }.onFailure {
            collector.push(message, it)
        }.getOrNull()

        private infix fun <T> T.logging(message: String) = also {
            collector.push(message)
        }

        private fun ErrorHandler.throwIfHasErrors(): Nothing? {
            if (errors.isEmpty()) return null
            val path = dirs.root.resolve(source)
            throw Exception("Failed to parse ${path.absolutePathString()}").apply {
                errors.forEach { addSuppressed(Exception(it.join())) }
            }
        }
    }

    fun process(): () -> Unit {
        if (dirs.temp.exists()) dirs.temp.deleteRecursively()

        val errors = mutableMapOf<Path, Throwable>()
        dirs.root.walk().forEach {
            processEntry(it)?.also { e -> errors[it] = e }
        }

        if (errors.isNotEmpty()) composeErrors(dirs.root, errors)
        return ::applyTemp
    }

    private fun processEntry(file: Path): Throwable? {
        val relative = dirs.root.relativize(file)
        val processor = EntryProcessor(relative)
        val result = runCatching {
            processor.process()
        }.onFailure {
            processor.collector.release()
            return it
        }.getOrThrow()

        if (result != null) statistics.processed.getAndIncrement()
        return runCatching {
            saveToTemp(relative, result, file)
        }.also {
            processor.collector.release()
        }.exceptionOrNull()
    }

    private fun saveToTemp(relative: Path, it: String?, file: Path) {
        val dest = dirs.temp.resolveChecked(relative)
        if (it != null) dest.writeText(it, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        else if (!inPlace) file.copyTo(dest, true)
    }

    private fun composeErrors(root: Path, errors: Map<Path, Throwable>): Nothing {
        throw Exception("Failed to process files in ${root.absolutePathString()}").apply {
            errors.forEach { (file, err) ->
                Exception("Failed to process ${file.absolutePathString()}").let {
                    it.addSuppressed(err)
                    addSuppressed(it)
                }
            }
        }
    }

    private fun applyTemp() {
        if (dirs.temp.notExists()) return
        dirs.dest.createDirectories()
        dirs.temp.copyToRecursively(dirs.dest, followLinks = false, overwrite = true, onError = { from, to, e ->
            throw IOException("Failed to copy $from to $to", e)
        })
        dirs.temp.deleteRecursively()
    }

    private fun updateParameters(): Boolean {
        val cache = dirs.outputCache.resolve("transform_parameters.yml")
        val collector = LogCollector(LOGGER, cache)

        val saved = if (!cache.isAvailable()) null
        else cache.inputStream().useCatching {
            YAML.decodeFromStream<TransformParameters>(it)
        }.onFailure {
            collector.push("Failed to read transform parameters", it)
        }.getOrNull()

        return if (saved != null && saved == params) true.also {
            collector.push("Cached parameters matched")
            collector.release()
        }
        else false.also {
            collector.push("Saving parameter caches")
            cache.parent.createDirectories()
            cache.writeConfigured { YAML.encodeToStream(params, it) }.onFailure {
                collector.push("Failed to save transform parameters", it)
            }
            collector.release()
        }
    }
}