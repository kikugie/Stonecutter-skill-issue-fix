package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.ProjectBranch
import dev.kikugie.stonecutter.data.StitcherParameters
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction0
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

    private val statistics: ProcessStatistics = ProcessStatistics()

    @TaskAction
    fun run() {
        val callbacks = mutableListOf<Callback>()
        val errors = mutableListOf<Throwable>()
        val time = measureTimeMillis {
            for ((branch, path) in sources.get()) processBranch(branch, path)
                ?.onSuccess { callbacks.add(it) }
                ?.onFailure { errors.add(it) }
        }
        if (errors.isEmpty()) runBlocking {
            for (it in callbacks) runCatching { it() }
                .onFailure { errors.add(it) }
        }
        if (errors.isNotEmpty())
            composeErrors(errors)

        val message = buildString {
            append("Switched to ${toVersion.get().project} in ${time}ms.")
            append(" ")
            append("(${statistics.total.get()} total | ${statistics.parsed.get()} parsed | ${statistics.total.get() - statistics.processed.get()} skipped)")
        }
        println(message)
    }

    private fun composeErrors(errors: Iterable<Throwable>): Nothing {
        throw Exception("Failed to switch from ${fromVersion.get().project} to ${toVersion.get().project}").apply {
            errors.forEach(::addSuppressed)
        }
    }

    private fun processBranch(branch: ProjectBranch, path: Path): Result<Callback>? {
        val params = data.get()[branch] ?: return null
        val dirs = DirectoryData(
            path.resolve(input.get()),
            path.resolve(output.get()),
            cacheDir.get()(branch, fromVersion.get()),
            cacheDir.get()(branch, toVersion.get()),
        )
        val processor = FileProcessor(
            dirs,
            params.toFileFilter(),
            Charsets.UTF_8,
            CommentRecognizers.DEFAULT,
            params.toTransformParams(toVersion.get().version),
            statistics, false
        )
        val message = buildString {
            appendLine("Processing branch ${branch.path}...")
            appendLine("  Root: ${dirs.root}")
            appendLine("  Dest: ${dirs.dest}")
            appendLine("  Cache in: ${dirs.inputCache}")
            appendLine("  Cache out: ${dirs.outputCache}")
        }
        project.logger.debug(message)
        return processor.runCatching {
            process()
        }
    }
}