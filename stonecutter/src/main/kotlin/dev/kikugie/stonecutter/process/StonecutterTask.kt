package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.BuildParameters
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.controller.storage.GlobalParameters
import dev.kikugie.stonecutter.data.tree.LightBranch
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.system.measureTimeMillis

/**Task used by Stonecutter to transform files.*/
abstract class StonecutterTask : DefaultTask() {
    /**Stonecutter project to switch from.*/
    @get:Input
    abstract val fromVersion: Property<StonecutterProject>

    /**Stonecutter project to switch to.*/
    @get:Input
    abstract val toVersion: Property<StonecutterProject>

    /**Input directory relative to each [sources] entry.*/
    @get:Input
    abstract val input: Property<String>

    /**Output directory relative to each [sources] entry.*/
    @get:Input
    abstract val output: Property<String>

    /**Root directories for all processed branches.*/
    @get:Input
    abstract val sources: MapProperty<LightBranch, Path>

    /**Build parameters for all processed branches.*/
    @get:Input
    abstract val data: MapProperty<LightBranch, BuildParameters>

    /**
     * Root directory provider for each version.
     * This is usually node's `build/stonecutter-cache`, however if it's not available
     * branch's `build/stonecutter-cache/out-of-bounds/$project` is used.
     */
    @get:Input
    abstract val cacheDir: Property<(LightBranch, StonecutterProject) -> Path>

    /**Parameters set by [StonecutterController].*/
    @get:Input
    abstract val params: Property<GlobalParameters>

    private val statistics: ProcessStatistics = ProcessStatistics()

    /**
     * Transforms the given branches. If no errors were reported, applies the changes,
     * otherwise prints the errors and throws and exception.
     */
    @TaskAction
    fun run() {
        if (!params.get().process) println("Switched to ${toVersion.get().project} (skipped file processing)")
            .also { return }
        val callbacks = mutableListOf<() -> Unit>()
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
        if (errors.isNotEmpty()) {
            printErrors(*errors.toTypedArray())
            throw Exception("Failed to switch from ${fromVersion.get().project} to ${toVersion.get().project}")
        }

        val message = buildString {
            append("Switched to ${toVersion.get().project} in ${time}ms.")
            append(" (")
            append("${statistics.total} total")
            append(" | ")
            append("${statistics.processed} processed")
            append(" | ")
            append("${statistics.total - statistics.processed} skipped")
            append(")")
        }
        println(message)
    }

    private fun processBranch(branch: LightBranch, path: Path): Result<() -> Unit>? {
        val buildParams = data.get()[branch] ?: return null
        val processParams = object : ProcessParameters {
            override val directory = DirectoryData(
                path.resolve(input.get()),
                path.resolve(output.get()),
                cacheDir.get()(branch, fromVersion.get()),
                cacheDir.get()(branch, toVersion.get()),
            )
            override val filter = buildParams.toFileFilter()
            override val parameters = buildParams.toTransformParams(toVersion.get().version, params.get().receiver)
            override val statistics = ProcessStatistics()
            override val recognizers = CommentRecognizers.DEFAULT
            override val charset = Charsets.UTF_8
            override val debug = params.get().debug
            override val cache = false

        }
        val processor = FileProcessor(processParams)
        val message = buildString {
            appendLine("Processing branch ${branch.location}...")
            appendLine("  Root: ${processParams.directory.root}")
            appendLine("  Dest: ${processParams.directory.dest}")
            appendLine("  Cache in: ${processParams.directory.inputCache}")
            appendLine("  Cache out: ${processParams.directory.outputCache}")
        }
        project.logger.debug(message)
        return processor.runCatching {
            process()
        }
    }
}