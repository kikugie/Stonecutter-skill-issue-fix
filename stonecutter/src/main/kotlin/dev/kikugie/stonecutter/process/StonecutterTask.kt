package dev.kikugie.stonecutter.process

import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.StonecutterPlugin
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.container.ConfigurationService
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import dev.kikugie.stonecutter.data.tree.LightBranch
import dev.kikugie.stonecutter.invoke
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.system.measureTimeMillis

abstract class StonecutterTask : DefaultTask() {
    @get:Internal abstract val parameters: Property<ConfigurationService.Snapshot>

    @get:Input abstract val instance: Property<ProjectHierarchy>

    @get:Input abstract val fromVersion: Property<StonecutterProject>
    @get:Input abstract val toVersion: Property<StonecutterProject>

    @get:Input abstract val input: Property<String>
    @get:Input abstract val output: Property<String>
    @get:Input abstract val sources: ListProperty<LightBranch>

    private val statistics: ProcessStatistics = ProcessStatistics()

    @TaskAction
    fun run() {
        val globalParameters = requireNotNull(parameters().global[instance()]) {
            "Missing global parameters for project ${instance()}: [${parameters().global.keys.joinToString()}]"
        }
        if (!globalParameters.process) return logger.lifecycle("Switched to ${instance()} (skipped file processing)")
        val callbacks = mutableListOf<() -> Unit>()
        val errors = mutableListOf<Throwable>()
        val time = measureTimeMillis {
            for (branch in sources.get()) process(branch)
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

        val message = "Switched to %s in %sms. (%d total | %d processed | %d skipped)".format(
            toVersion().project,
            time,
            statistics.total,
            statistics.processed,
            statistics.total - statistics.processed
        )
        logger.lifecycle(message)
    }

    private fun process(branch: LightBranch): Result<() -> Unit>? {
        val globalParameters: GlobalParameters = parameters().global[instance()] ?: return null
        val buildParameters: BuildParameters = parameters().build[branch.hierarchy + toVersion().project] ?: return null
        val processParameters = ProcessParameters(
            dirs = DirectoryData(
                input = branch.location.resolve(input()),
                output = branch.location.resolve(output()),
                debug = branch[fromVersion().project]!!.location.resolve("build/stonecutter-cache/debug"),
                temp = branch[toVersion().project]!!.location.resolve("build/stonecutter-cache/temp")
            ),
            filter = buildParameters.toFileFilter(),
            parameters = buildParameters.toTransformParameters(toVersion().version, globalParameters.receiver),
            recognizers = CommentRecognizers.DEFAULT,
            statistics = statistics,
            charset = Charsets.UTF_8,
            debug = globalParameters.debug
        )
        return FileProcessor(processParameters).runCatching { process() }
    }

    companion object {
        private fun BuildParameters.toFileFilter(): (Path) -> Boolean = { file ->
            file.extension !in excludedExtensions && file !in excludedPaths && excludedPaths.none {
                it.isDirectory() && file.startsWith(
                    it
                )
            }
        }

        private fun BuildParameters.toTransformParameters(version: String, key: String) = with(dependencies) {
            getOrElse(key) { VersionParser.parseLenient(version).value }.let {
                put(key, it)
                put("", it)
            }
            TransformParameters(swaps, constants, this)
        }

        private fun printErrors(vararg errors: Throwable): Unit = printErrors(0, *errors)
        private fun printErrors(indent: Int, vararg errors: Throwable): Unit = errors.forEach {
            buildString {
                appendLine("${it.message}".prependIndent('\t' * indent))
                if (it !is ProcessException) it.stackTrace.forEach { trace -> appendLine("${'\t' * (indent + 2)}at $trace") }
            }.let(::printErr)
            it.cause?.let { cause -> printErrors(indent + 1, cause) }
            it.suppressed.forEach { suppressed -> printErrors(indent + 1, suppressed) }
        }

        private fun printErr(any: Any) = System.err.print(any)
        private operator fun Char.times(n: Int) = if (n <= 0) "" else buildString { repeat(n) { append(this@times) } }
    }
}