package dev.kikugie.stonecutter

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitcher.transformer.TransformParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
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
    abstract val debug: Property<Boolean>

    @get:Input
    abstract val input: Property<Path>

    @get:Input
    abstract val output: Property<Path>

    @get:Input
    abstract val fromVersion: Property<StonecutterProject>

    @get:Input
    abstract val toVersion: Property<StonecutterProject>

    @get:Input
    abstract val constants: MapProperty<String, Boolean>

    @get:Input
    abstract val swaps: MapProperty<String, String>

    @get:Input
    abstract val dependencies: MapProperty<String, SemanticVersion>

    @get:Input
    abstract val filter: Property<FileFilter>

    private lateinit var manager: FileManager
    private val transformed = AtomicInteger(0)
    private val total = AtomicInteger(0)

    init {
        chiseled.convention(false)
        debug.convention(false)
    }

    @TaskAction
    fun run() {
        if (!input.isPresent || !output.isPresent || !toVersion.isPresent)
            throw IllegalArgumentException("[Stonecutter] StonecutterTask is not fully initialized")
        manager = createManager()
        val time = measureTimeMillis {
            transform(input.get(), output.get())
        }
        println("[Stonecutter] Switched to ${toVersion.get().project} in ${time}ms (${transformed.get()}/${total.get()} modified)")
    }

    private fun createManager(): FileManager {
        val dest = if (chiseled.get()) project.parent!! else project
        fun cacheDir(pr: StonecutterProject) = dest.project(pr.project).buildDirectory.toPath().resolve("stonecutterCache")
        val deps = dependencies.get().toMutableMap()
        val mcVersion = deps["minecraft"] ?: SemanticVersionParser.parse(toVersion.get().version)
        deps["minecraft"] = mcVersion
        deps[""] = mcVersion

        val params = TransformParameters(swaps.get(), constants.get(), deps)
        return FileManager(
            inputCache = cacheDir(fromVersion.get()),
            outputCache = cacheDir(toVersion.get()),
            filter = filter.get(),
            recognizers = listOf(StandardMultiLine, StandardSingleLine),
            params = params,
            debug = debug.get()
        )
    }

    private fun transform(input: Path, output: Path): Unit = runBlocking {
        val inPlace = input == output
        val skipped = mutableListOf<Path>()
        val processed = mutableListOf<Pair<Path, String>>()
        val exceptions = mutableListOf<Throwable>()
        input.walk().map {
            total.incrementAndGet()
            it to process(input, input.relativize(it))
        }.asFlow().flowOn(Dispatchers.Default).catch {
            exceptions += it
        }.transform<Pair<Path, String?>, Unit> { (file, result) ->
            if (result != null) transformed.incrementAndGet()
            if (result == null) skipped.add(file)
            else processed.add(file to result)
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
                if (debug.get() && err !is SyntaxException) cause?.stackTrace?.forEach {
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