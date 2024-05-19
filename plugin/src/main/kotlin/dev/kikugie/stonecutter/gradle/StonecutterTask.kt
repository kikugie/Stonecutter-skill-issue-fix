package dev.kikugie.stonecutter.gradle

import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.Transformer
import dev.kikugie.stitcher.process.access.Expression
import dev.kikugie.stitcher.process.access.ExpressionProcessor
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.recognizer.StandardSingleLine
import dev.kikugie.stitcher.process.transformer.ConditionVisitor
import dev.kikugie.stonecutter.cutter.FileManager
import dev.kikugie.stonecutter.util.buildDirectory
import dev.kikugie.stonecutter.version.FabricVersionChecker
import dev.kikugie.stonecutter.version.McVersionExpression
import dev.kikugie.stonecutter.version.VersionChecker
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.system.measureTimeMillis

@Suppress("LeakingThis")
@OptIn(ExperimentalPathApi::class)
internal abstract class StonecutterTask : DefaultTask() {
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
    abstract val expressions: ListProperty<Expression>

    @get:Input
    abstract val swaps: MapProperty<String, String>

    @get:Input
    abstract val filter: Property<(Path) -> Boolean>

    @get:Input
    abstract val versionChecker: Property<(Project) -> VersionChecker>

    @get:Internal
    internal lateinit var conditionProcessor: ConditionVisitor

    @TaskAction
    fun run() {
        if (!input.isPresent || !output.isPresent || !toVersion.isPresent)
            throw IllegalArgumentException("[Stonecutter] StonecutterTask is not fully initialized")
        val expressionsImpl = buildList {
            val checker = versionChecker.get()(project)
            val target = checker.parseVersion(toVersion.get().version)

            addAll(expressions.get())
            add(McVersionExpression(target, checker))
        }
        conditionProcessor = ConditionVisitor(ExpressionProcessor(constants.get(), expressionsImpl))
        val time = measureTimeMillis {
            transform(input.get(), output.get(), filter.get())
        }
        println("[Stonecutter] Switched to ${toVersion.get().project} in ${time}ms")
    }

    init {
        versionChecker.convention { FabricVersionChecker.create(it) }
    }

    private fun transform(input: Path, output: Path, filter: (Path) -> Boolean) {
        val inPlace = input == output
        val skipped = mutableListOf<Path>()
        val processed = mutableListOf<Pair<Path, String>>()

        for (file in input.walk()) {
            val result = process(input, file)
            if (result == null) skipped.add(file)
            else processed.add(file to result)
        }
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
                StandardCharsets.ISO_8859_1,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }

    private fun process(root: Path, file: Path): String? = try {
        val recognizers = listOf(StandardMultiLine, StandardSingleLine)
        val relative = root.relativize(file.parent)
        val subproject = project.let { it.parent ?: it }.project(":${fromVersion.get().project}")
        val cacheDir = subproject.buildDirectory.resolve("stonecutterCache").resolve(relative.toFile()).toPath()
        Files.createDirectories(cacheDir)
        FileManager.processFile(
            file,
            filter.get(),
            StandardCharsets.ISO_8859_1,
            recognizers,
            conditionProcessor,
            swaps.get(),
            cacheDir
        )
    } catch (e: Exception) {
        throw RuntimeException("Failed to process $file").apply {
            initCause(e)
        }
    }
}