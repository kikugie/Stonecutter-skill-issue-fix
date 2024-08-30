package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.ProjectBranch
import dev.kikugie.stonecutter.data.StitcherParameters
import dev.kikugie.stonecutter.process.ProcessResult.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.*
import kotlin.system.measureTimeMillis

internal abstract class StonecutterTask : DefaultTask() {
    @get:Input
    abstract val fromVersion: Property<StonecutterProject>

    @get:Input
    abstract val toVersion: Property<StonecutterProject>

    @get:Input
    abstract val input: Property<String>

    @get:Input
    abstract val output: Property<String>

    @get:Input
    abstract val sources: MapProperty<ProjectBranch, Path>

    @get:Input
    abstract val data: MapProperty<ProjectBranch, StitcherParameters>

    @get:Input
    abstract val cacheDir: Property<(ProjectBranch, StonecutterProject) -> Path>

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val statistics = Statistics()

    @TaskAction
    fun run() {
        statistics.duration = measureTimeMillis { transform(sources.get()) }
        println(
            """
            [Stonecutter] Switched to ${toVersion.get().project} in ${statistics.duration}ms 
            (${statistics.total - statistics.skipped} processed | ${statistics.parsed} parsed | ${statistics.total} total)
            """.trimIndent()
        )
    }

    private fun transform(dests: Map<ProjectBranch, Path>) = runBlocking {
        val errored = AtomicReference(false)
        val merged = dests.map { (branch, path) ->
            val input = path.resolve(input.get())
            val output = path.resolve(output.get())
            logger.debug(
                "Transforming '{}': {} ({}) -> {} ({}) with {}",
                branch.id,
                input,
                fromVersion.get(),
                output,
                toVersion.get(),
                data.get()[branch]?.toParams(toVersion.get().version)
            )
            transform(branch, input, output, errored)
        }.merge().flowOn(Dispatchers.IO).toList()
        statistics.total = merged.size

        val error = errored.get()
        for ((result, output) in merged) when (result) {
            is TransformResult.Processed -> {
                statistics.parsed++
                if (output == null || error) continue
                output.parent.createDirectories()
                output.writeText(
                    result.str,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }

            is TransformResult.Skipped -> {
                statistics.skipped++
                if (output == null || error) continue
                output.parent.createDirectories()
                result.file.copyTo(output, true)
            }

            is TransformResult.Failed -> {
                System.err.println(result.error.message)
            }
        }
        if (error) throw Exception("Failed to switch to ${toVersion.get().project}")
    }

    @OptIn(ExperimentalPathApi::class)
    private fun transform(
        project: ProjectBranch,
        input: Path,
        output: Path,
        marker: AtomicReference<Boolean>
    ): Flow<Pair<TransformResult, Path?>> {
        val manager = FileManager(project)
        return input
            .walk()
            .map {
                val result = process(manager, input, it)
                if (result is TransformResult.Failed) marker.set(true)
                result to output.resolve(input.relativize(it))
            }
            .asFlow()
    }

    @Suppress("LoggingSimilarMessage")
    private fun process(manager: FileManager, root: Path, file: Path): TransformResult = runCatching {
        manager.process(root, root.relativize(file))
    }.run {
        if (isFailure) TransformResult.Failed(file, exceptionOrNull()!!).also {
            println(it)
        }
        else when(val result = getOrThrow()) {
            CacheMatches, FilterExcluded, NewMatches -> TransformResult.Skipped(file).also {
                logger.debug("{} - {}", it, result::class.simpleName)
            }
            is NewProcessed -> TransformResult.Processed(file, result.content).also {
                logger.debug("Processed({}) - {}", it.file, result::class.simpleName)
            }
            is ResultCached -> TransformResult.Processed(file, result.content).also {
                logger.debug("Processed({}) - {}", it.file, result::class.simpleName)
            }
        }
    }

    private fun FileManager(branch: ProjectBranch): FileManager {
        val data = checkNotNull(data.get()[branch]) {
            "Project '$project' not found in [${data.get().keys.joinToString { "'$it'" }}]"
        }
        return FileManager(
            inputCache = cacheDir.get()(branch, fromVersion.get()),
            outputCache = cacheDir.get()(branch, toVersion.get()),
            filter = FileFilter(data.excludedExtensions, data.excludedPaths),
            recognizers = listOf(StandardMultiLine, StandardSingleLine),
            params = data.toParams(toVersion.get().version),
            debug = false,
            statistics = statistics
        )
    }
}