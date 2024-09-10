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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.*

@OptIn(ExperimentalSerializationApi::class, ExperimentalPathApi::class)
class FileProcessor(
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
        val FILE_LOCKS = ConcurrentHashMap<Path, Mutex>()
        const val CHUNK = 1024 * 16
        const val SEED: Long = -0x701074A728960DCB
    }

    @OptIn(ExperimentalEncodingApi::class)
    inner class EntryProcessor(private val source: Path) {
        internal val collector = LogCollector(LOGGER, dirs.root.resolve(source))

        internal suspend fun process(): String? {
            statistics.total.getAndIncrement()
            if (!filter.shouldProcess(source)) return null.also {
                collector.push("Skipping, filtered")
            }

            val text = withContext(Dispatchers.IO) { dirs.root.resolve(source).readText(charset) }
            val checksum = createChecksum(text)
            val checksumsMatch = checkChecksum(checksum)
            if (parametersMatch && checksumsMatch && !debug) readCachedOutput()?.also {
                return if (it == text) null.also {
                    collector.push("Skipping, matches cache")
                } else it.also {
                    collector.push("Found output cache")
                }
            }

            var ast: Scope? = null
            if (checksumsMatch && !debug) ast = readCachedAst()?.also {
                collector.push("Found AST cache")
            } ?: null.also {
                collector.push("AST cache not found")
            }
            val handler = StoringErrorHandler()
            if (ast == null) {
                statistics.parsed.getAndIncrement()
                val parser = FileParser.create(text, handler, recognizers, params)
                ast = parser.parse()
                collector.push("Parsed AST, ${handler.errors.size} errors")
                handler.throwIfHasErrors()
                writeCachedAst(ast)
                writeDebugAst(ast)
            }
            Transformer(ast, recognizers, params, handler).process()
            collector.push("Transformed AST, ${handler.errors.size} errors")
            handler.throwIfHasErrors()
            val result = ast.join()
            writeCachedOutput(result)
            writeChecksum(checksum)
            return if (result == text) null.also {
                collector.push("Skipping, matches input")
            } else result.also {
                collector.push("Successfully processed")
            }
        }

        private suspend fun writeDebugAst(ast: Scope) = withContext(Dispatchers.IO) {
            val file = dirs.debug.resolve(source)
            file.createParentDirectories().writeConfigured {
                YAML.encodeToStream(ast, it)
            }.onFailure {
                collector.push("Failed to save debug AST", it)
            }
        }

        private suspend fun writeCachedAst(ast: Scope) = withContext(Dispatchers.IO) {
            val file = dirs.asts.resolve(source)
            file.createParentDirectories().writeConfigured {
                it.write(Cbor.Default.encodeToByteArray(ast))
            }.onFailure {
                collector.push("Failed to save cached AST", it)
            }
        }

        private suspend fun writeCachedOutput(result: String) = withContext(Dispatchers.IO) {
            val file = dirs.results.resolve(source)
            runCatching {
                file.createParentDirectories().writeText(result, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }.onFailure {
                collector.push("Failed to save cached output", it)
            }
        }

        private suspend fun readCachedAst(): Scope? = withContext(Dispatchers.IO) {
            val file = dirs.asts.resolve(source)
            if (!file.isAvailable()) null.also {
                collector.push("AST cache not found")
            } else file.inputStream().useCatching {
                file.mutex().withLock { Cbor.Default.decodeFromByteArray<Scope>(it.readBytes()) }
            }.onFailure {
                collector.push("Failed to read cached AST", it)
            }.getOrNull()
        }

        private suspend fun readCachedOutput(): String? = withContext(Dispatchers.IO) {
            val file = dirs.results.resolve(source)
            if (!file.isAvailable()) null.also {
                collector.push("Output cache not found")
            } else runCatching {
                file.mutex().withLock { file.readText(charset) }
            }.onFailure {
                collector.push("Failed to read cached output", it)
            }.getOrNull()
        }

        private suspend fun writeChecksum(checksum: ByteArray) = withContext(Dispatchers.IO) {
            val root = dirs.inputCache.resolve("checksums")
            val dest = root.resolve(source.withExtension("checksum"))
            dest.createParentDirectories().runCatching {
                writeBytes(checksum, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }.onFailure {
                collector.push("Failed to write checksum", it)
            }
        }

        private suspend fun checkChecksum(current: ByteArray): Boolean =
            withContext(Dispatchers.IO) {
                val root = dirs.inputCache.resolve("checksums")
                val file = root.resolve(source.withExtension("checksum"))
                if (!file.isAvailable()) return@withContext false.also {
                    collector.push("Checksum file not found")
                }

                val checksum = runCatching {
                    file.mutex().withLock { file.readBytes() }
                }.onFailure {
                    collector.push("Failed to read checksum", it)
                }.getOrNull()
                current.contentEquals(checksum).also {
                    if (!it) buildString {
                        appendLine("Checksum mismatch:")
                        appendLine("  Expected - ${Base64.encode(current)}")
                        append("  Received - ${checksum?.let(Base64::encode)}")
                    }.let(collector::push)
                }
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

        private fun ErrorHandler.throwIfHasErrors() {
            if (errors.isEmpty()) return
            val path = dirs.root.resolve(source)
            throw Exception("Failed to parse ${path.absolutePathString()}").apply {
                errors.forEach { addSuppressed(Exception(it.join())) }
            }
        }
    }

    fun process()  {
        dirs.temp.deleteRecursively()
        val errors: MutableMap<Path, MutableList<Throwable>> = mutableMapOf()
        for (file in dirs.root.walk()) runBlocking {
            val relative = dirs.root.relativize(file)
            val processor = EntryProcessor(relative)
            runCatching {
                processor.process()
            }.mapCatching {
                if (it != null) statistics.processed.getAndIncrement()
                withContext(Dispatchers.IO) {
                    val dest = dirs.temp.resolve(relative)
                    if (it != null) dest.createParentDirectories()
                        .writeText(it, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    else if (!inPlace) file
                        .copyTo(dest.createParentDirectories(), true)
                }
            }.onFailure {
                errors.getOrPut(file, ::mutableListOf).add(it)
            }
            processor.collector.release()
        }

        if (errors.isNotEmpty()) composeErrors(dirs.root, errors)
        runBlocking {
            applyTemp()
        }
    }

    private fun composeErrors(root: Path, errors: Map<Path, List<Throwable>>): Nothing {
        throw Exception("Failed to process files in ${root.absolutePathString()}").apply {
            errors.forEach { (file, list) ->
                Exception("Failed to process ${file.absolutePathString()}").apply {
                    list.forEach(::addSuppressed)
                }.let(::addSuppressed)
            }
        }
    }

    private suspend fun applyTemp() = withContext(Dispatchers.IO) {
        if (dirs.temp.notExists()) return@withContext
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

    private inline fun Nothing?.also(block: () -> Unit): Nothing? {
        block()
        return this
    }

    private fun Path.createParentDirectories() = also {
        parent.createDirectories()
    }

    private fun Path.mutex() = FILE_LOCKS.getOrPut(this, ::Mutex)

    private fun Long.toByteList(dest: MutableList<Byte>) {
        (0 until 8).forEach { i ->
            dest.add((this shr (i * 8) and 0xFF).toByte())
        }
    }

    private fun Path.withExtension(ext: String) = parent.resolve("${fileName.nameWithoutExtension}.$ext")

    private inline fun Path.writeConfigured(block: (OutputStream) -> Unit) =
        outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).useCatching {
            block(it)
        }
}