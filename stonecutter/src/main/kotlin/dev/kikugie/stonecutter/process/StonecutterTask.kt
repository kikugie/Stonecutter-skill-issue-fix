package dev.kikugie.stonecutter.process

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.buildDirectory
import dev.kikugie.stonecutter.configuration.StonecutterDataView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.walk
import kotlin.io.path.writeText
import kotlin.system.measureTimeMillis

@Suppress("LeakingThis")
@OptIn(ExperimentalPathApi::class)
internal abstract class StonecutterTask : DefaultTask() {
    @get:Input
    abstract val chiseled: Property<Boolean>

    @get:Input
    abstract val input: Property<Path>

    @get:Input
    abstract val output: Property<Path>

    @get:Input
    abstract val fromVersion: Property<StonecutterProject>

    @get:Input
    abstract val toVersion: Property<StonecutterProject>

    @get:Input
    abstract val data: Property<StonecutterDataView>

    private lateinit var manager: FileManager
    private val statistics = Statistics()

    init {
        chiseled.convention(false)
    }

    @TaskAction
    fun run() {
        require(input.isPresent && output.isPresent && toVersion.isPresent) {
            "[Stonecutter] StonecutterTask is not fully initialized"
        }
        manager = createManager()
        statistics.duration = measureTimeMillis {
            transform(input.get(), output.get())
        }
        println("[Stonecutter] Switched to ${toVersion.get().project} in ${statistics.duration}ms (${statistics.total - statistics.skipped}/${statistics.total} modified)")
    }

    private fun createManager(): FileManager {
        val dataView = data.get()
        val dest = if (chiseled.get()) project.parent!! else project
        fun cacheDir(pr: StonecutterProject) = dest.project(pr.project).buildDirectory.toPath().resolve("stonecutterCache")

        val params = dataView.toParams(toVersion.get().version)
        return FileManager(
            inputCache = cacheDir(fromVersion.get()),
            outputCache = cacheDir(toVersion.get()),
            filter = FileFilter(dataView.excludedExtensions, dataView.excludedPaths),
            recognizers = listOf(StandardMultiLine, StandardSingleLine),
            params = params,
            debug = dataView.debug,
            statistics = statistics
        )
    }

    private fun transform(input: Path, output: Path): Unit = runBlocking {
        val inPlace = input == output
        val skipped = mutableListOf<Path>()
        val processed = mutableListOf<Pair<Path, String>>()
        val exceptions = mutableListOf<Throwable>()
        input.walk().map {
            statistics.total += 1
            it to process(input, input.relativize(it))
        }.asFlow().flowOn(Dispatchers.Default).catch {
            exceptions += it
        }.transform<Pair<Path, String?>, Unit> { (file, result) ->
            if (result != null) processed.add(file to result)
            else {
                statistics.skipped += 1
                skipped.add(file)
            }
        }.collect()
        if (exceptions.isNotEmpty())
            throw exceptions.composeCauses()
        if (!inPlace) skipped.forEach {
            val out = output.resolve(input.relativize(it))
            Files.createDirectories(out.parent)
            it.copyTo(out)
        }
        processed.forEach { (it, content) ->
            val out = output.resolve(input.relativize(it))
            Files.createDirectories(out.parent)
            out.writeText(
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }

    private fun List<Throwable>.composeCauses(): Throwable {
        val cause = buildString {
            for (err in this@composeCauses) {
                val primary = err.message ?: "An error occurred"
                val cause = err.cause
                val message = StringBuilder()
                message.append("    > $primary:\n")
                for (line in (cause?.message ?: "").lines())
                    message.append("        $line\n")
                if (data.get().debug && err !is SyntaxException) cause?.stackTrace?.forEach {
                    message.append("            $it\n")
                }
                append(message)
            }
        }
        return RuntimeException("Failed to switch to ${toVersion.get().project}:\n$cause")
    }

    private fun process(root: Path, file: Path): String? = try {
        manager.process(root, file)
    } catch (e: Exception) {
        throw RuntimeException("Failed to process $file").apply {
            initCause(e)
        }
    }
}