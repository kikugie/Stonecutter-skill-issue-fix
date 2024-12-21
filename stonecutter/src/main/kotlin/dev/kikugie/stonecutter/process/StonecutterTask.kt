package dev.kikugie.stonecutter.process

import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.container.ConfigurationService
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import dev.kikugie.stonecutter.data.tree.LightBranch
import dev.kikugie.stonecutter.data.tree.BranchPrototype
import dev.kikugie.stonecutter.invoke
import dev.kikugie.stonecutter.then
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.system.measureTimeMillis

/**Task responsible for scanning versioned comments and modifying files to match the given version.*/
@PublishedApi
internal abstract class StonecutterTask : DefaultTask() {
    /**
     * All collected build parameters.
     * *This should've been done with a build service, but fate was against it.*
     */
    @get:Input abstract val parameters: Property<ConfigurationService.Snapshot>

    /**Any key present in [parameters]. Used to retrieve [GlobalParameters].*/
    @get:Input abstract val instance: Property<ProjectHierarchy>

    /**Current version, before the switch is done.*/
    @get:Input abstract val fromVersion: Property<StonecutterProject>

    /**Version to be used after the switch, which is also used to retrieve [BuildParameters].*/
    @get:Input abstract val toVersion: Property<StonecutterProject>

    /**Source directory relative to each [BranchPrototype.location] in [sources].*/
    @get:Input abstract val input: Property<String>

    /**File destination directory relative to each [BranchPrototype.location] in [sources].*/
    @get:Input abstract val output: Property<String>

    /**Branches to be processed by this task.*/
    @get:Input abstract val sources: ListProperty<LightBranch>

    private val statistics: ProcessStatistics = ProcessStatistics()
    private var encounteredSymlink = false

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

    private fun reportSymlink() {
        if (encounteredSymlink) return else encounteredSymlink = true
        """
            Encountered symbolic links during file processing.
            Stonecutter will skip processing them because it can cause unexpected behavior.
        """.trimIndent().let(logger::error)
    }

    private fun BuildParameters.allowFile(file: Path): Boolean = file.invariantSeparatorsPathString.let { path ->
        require(path.startsWith("src/")) { "Encountered non-source file $path" } // TODO: Testing
        return when {
            file.isSymbolicLink() -> false.also { reportSymlink(); logger.warn("Skipping symbolic link: $path") }
            file.isDirectory() -> path !in exclusions
            file.isRegularFile() -> file.extension in extensions && path !in exclusions
            else -> false.also { logger.warn("Unknown file type: $path") }
        }
    }

    private fun BuildParameters.toFileFilter(): (Path) -> Boolean = { allowFile(it) }

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