package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.ProjectBranch
import dev.kikugie.stonecutter.data.StitcherParameters
import groovy.lang.Reference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
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
        val errored = Reference(false)
        val merged = dests.map { (project, path) ->
            transform(project, path.resolve(input.get()), path.resolve(output.get()), errored)
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
        marker: Reference<Boolean>
    ): Flow<Pair<TransformResult, Path?>> {
        val out = if (input == output) null else output
        val manager = FileManager(project)
        return input
            .walk()
            .map {
                val result = process(manager, input, it)
                if (result is TransformResult.Failed) marker.set(true)
                result to out?.resolve(input.relativize(it))
            }
            .asFlow()
    }

    private fun process(manager: FileManager, root: Path, file: Path): TransformResult = runCatching {
        manager.process(root, root.relativize(file))
    }.run {
        when {
            isFailure -> TransformResult.Failed(file, exceptionOrNull()!!)
            getOrNull() == null -> TransformResult.Skipped(file)
            else -> TransformResult.Processed(file, getOrNull()!!)
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