package dev.kikugie.stonecutter.process

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.eval.join
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.exception.join
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stitcher.transformer.Transformer
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
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.*

private typealias ChecksumMap = MutableMap<String, ByteArray>

@OptIn(ExperimentalPathApi::class, ExperimentalEncodingApi::class, ExperimentalSerializationApi::class)
internal class FileProcessor(params: ProcessParameters) : ProcessParameters by params {
    private val inPlace = directory.root == directory.dest
    private val parametersMatch = updateParameters() && !debug
    private val checksums = readChecksumMap()

    internal companion object {
        val YAML = Yaml(
            configuration = YamlConfiguration(
                strictMode = false
            )
        )
        val LOGGER: Logger = LoggerFactory.getLogger(FileProcessor::class.java)
        val HASHER: XXHash64 = XXHashFactory.fastestInstance().hash64()
        const val CHUNK = 1024 * 16
        const val SEED: Long = -0x701074A728960DCB
        fun Path.withExtension(ext: String): Path =
            parent.resolve("${fileName.nameWithoutExtension}.$ext")

        fun Path.resolveChecked(subpath: Path): Path = resolve(subpath).apply {
            parent.createDirectories()
        }

        operator fun ByteArray.set(index: Int, value: Long) {
            for (i in 0 until 8) this[index + i] = (value shr (i * 8) and 0xFF).toByte()
        }
    }

    inner class EntryProcessor(private val source: Path) {
        internal val collector = LogCollector(LOGGER, directory.root.resolve(source), debug)

        fun process(): String? {
            statistics.total++
            if (!filter(source)) return null logging "Skipping, filtered"

            val text = directory.root.resolve(source).readText(charset)
            val checksum = if (cache) createChecksum(text, charset).also {ch ->
                val checksumsMatch = verifyChecksum(ch)
                if (parametersMatch && checksumsMatch && !debug) readCachedOutput()?.let {
                    checksums[source.invariantSeparatorsPathString] = ch
                    return if (it == text) null logging "Skipping, matches cache"
                    else it logging "Found output cache"
                }
            } else byteArrayOf()

            val handler = StoringErrorHandler()
            statistics.processed++
            val parser = FileParser.create(text, handler, recognizers, parameters)
            val ast = parser.parse().also {
                if (debug) writeDebugAst(it)
            }
            collector.push("Parsed AST, ${handler.errors.size} errors")
            handler.throwIfHasErrors()

            Transformer(ast, recognizers, parameters, handler).process()
            collector.push("Transformed AST, ${handler.errors.size} errors")
            handler.throwIfHasErrors()

            val result = ast.join()
            if (cache) writeCachedOutput(result)
            checksums[source.invariantSeparatorsPathString] = checksum
            return if (result == text) null logging "Skipping, matches input"
            else result logging "Successfully processed"
        }

        private fun readCachedOutput(): String? {
            val file = directory.results.resolve(source)
            return if (!file.isAvailable()) null logging "Output cache not found"
            else file.runReporting("Failed to read cached output") {
                readText(charset)
            }
        }

        private fun writeCachedOutput(result: String) = directory.results
            .resolveChecked(source)
            .runReporting("Failed to save cached output") {
                writeText(result, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }

        private fun writeDebugAst(ast: Scope) = directory.debug
            .resolveChecked(source.withExtension("yml"))
            .runReporting("Failed to save debug AST") {
                outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
                    YAML.encodeToStream(ast, it)
                }
            }

        @OptIn(ExperimentalEncodingApi::class)
        private fun verifyChecksum(current: ByteArray): Boolean {
            val cached = checksums[source.invariantSeparatorsPathString] ?: return false
            val match = current.contentEquals(cached)
            null logging if (match) "Checksums match" else buildString {
                appendLine("Checksum mismatch:")
                appendLine("  Expected - ${Base64.encode(current)}")
                append("  Received - ${Base64.encode(cached)}")
            }
            return match
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
            val path = directory.root.resolve(source)
            throw ProcessException("Failed to parse ${path.absolutePathString()}").apply {
                errors.forEach { addSuppressed(ProcessException(it.join())) }
            }
        }
    }

    fun process(): () -> Unit {
        if (directory.temp.exists()) directory.temp.deleteRecursively()

        val errors = mutableMapOf<Path, Throwable>()
        directory.root.walk().forEach {
            processEntry(it)?.also { e -> errors[it] = e }
        }

        writeChecksumMap()
        if (errors.isNotEmpty()) composeErrors(directory.root, errors)
        return ::applyTemp
    }

    private fun processEntry(file: Path): Throwable? {
        val relative = directory.root.relativize(file)
        val processor = EntryProcessor(relative)
        val result = try {
            processor.process()
        } catch (e: Exception) {
            processor.collector.release()
            return e
        }

        return runCatching {
            saveToTemp(relative, result, file)
        }.also {
            processor.collector.release()
        }.exceptionOrNull()
    }

    private fun saveToTemp(relative: Path, it: String?, file: Path) {
        val dest = directory.temp.resolveChecked(relative)
        if (it != null) dest.writeText(it, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        else if (!inPlace) file.copyTo(dest, true)
    }

    private fun composeErrors(root: Path, errors: Map<Path, Throwable>): Nothing =
        throw ProcessException("Failed to process files in ${root.absolutePathString()}").apply {
            for ((_, err) in errors) addSuppressed(err)
        }

    private fun applyTemp() {
        if (directory.temp.notExists()) return
        directory.dest.createDirectories()
        directory.temp.copyToRecursively(directory.dest, followLinks = false, overwrite = true, onError = { from, to, e ->
            throw IOException("Failed to copy $from to $to", e)
        })
    }

    private fun updateParameters(): Boolean {
        if (!cache) return false
        val cache = directory.outputCache.resolve("transform_parameters.yml")
        val collector = LogCollector(LOGGER, cache)

        val saved = if (!cache.isAvailable()) null
        else cache.inputStream().useCatching {
            YAML.decodeFromStream<TransformParameters>(it)
        }.onFailure {
            collector.push("Failed to read transform parameters", it)
        }.getOrNull()

        return if (saved != null && saved == parameters) true.also {
            collector.push("Cached parameters matched")
            collector.release()
        } else false.also {
            cache.parent.createDirectories()
            cache.outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).useCatching {
                YAML.encodeToStream(parameters, it)
            }.onFailure {
                collector.push("Failed to save transform parameters", it)
            }
            collector.release()
        }
    }

    private fun readChecksumMap(): ChecksumMap {
        if (!cache) return mutableMapOf()
        val file = directory.inputCache.resolve("checksums.cbor")
        val collector = LogCollector(LOGGER, file)
        val map: ChecksumMap = mutableMapOf()

        return if (!file.isAvailable()) map.also {
            collector.push("Checksums file not found")
        } else file.runCatching {
            val data: ChecksumMap = Cbor.Default.decodeFromByteArray(readBytes())
            if (debug) map else data.toMutableMap()
        }.onFailure {
            collector.push("Failed to read checksums file", it)
        }.getOrNull() ?: map
    }

    private fun writeChecksumMap() {
        if (!cache) return
        val file = directory.inputCache.resolve("checksums.cbor").apply {
            parent.createDirectories()
        }
        val collector = LogCollector(LOGGER, file)

        file.runCatching {
            writeBytes(
                Cbor.Default.encodeToByteArray(checksums),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }.onFailure {
            collector.push("Failed to write checksums file", it)
        }
    }
}