package dev.kikugie.stonecutter.cutter

import dev.kikugie.stonecutter.gradle.ProjectVersion
import dev.kikugie.stonecutter.processor.ConditionProcessor
import dev.kikugie.stonecutter.processor.Expression
import dev.kikugie.stonecutter.version.McVersionExpression
import dev.kikugie.stonecutter.version.FabricVersionChecker
import dev.kikugie.stonecutter.version.VersionChecker
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

@Suppress("LeakingThis")
abstract class StonecutterTask : DefaultTask() {
    @get:Input
    abstract val input: Property<Path>

    @get:Input
    abstract val output: Property<Path>

    @get:Input
    abstract val toVersion: Property<ProjectVersion>

    @get:Input
    abstract val fileFilter: Property<(Path) -> Boolean>

    @get:Input
    abstract val versionChecker: Property<(Project) -> VersionChecker>

    @get:Input
    abstract val debug: Property<Boolean>

    @get:Input
    abstract val expressions: ListProperty<Expression>

    @get:Internal
    internal lateinit var processor: ConditionProcessor

    init {
        fileFilter.convention { true }
        versionChecker.convention { FabricVersionChecker.create(it) }
        debug.convention(false)
        expressions.convention(emptyList())
    }

    @TaskAction
    @ExperimentalPathApi
    fun run() {
        if (!input.isPresent || !output.isPresent || !toVersion.isPresent)
            throw IllegalArgumentException("[Stonecutter] StonecutterTask is not fully initialized")
        processor = ConditionProcessor(collectExpressions())

        try {
            transform(input.get(), output.get())
        } catch (e: Exception) {
            throw RuntimeException("[Stonecutter] Failed to transform file:\n${e.message}", e)
        }
    }

    private fun collectExpressions(): List<Expression> {
        val exprs = mutableListOf<Expression>()
        if (debug.get()) {
            exprs += ConditionProcessor.TRUE
            exprs += ConditionProcessor.FALSE
        }
        val checker = versionChecker.get()(project)
        val target = checker.parseVersion(toVersion.get().version)
        exprs.addAll(expressions.get())
        exprs += McVersionExpression(target, checker)
        return exprs
    }


    @ExperimentalPathApi
    private fun transform(inputRoot: Path, outputRoot: Path): Boolean {
        if (inputRoot.notExists()) return false
        val processed = mutableListOf<Pair<Path, CharSequence>>()
        // Files without changes will be skipped or copied directly
        val matching = mutableListOf<Pair<Path, Path>>()
        // Collect files before writing in case an exception is thrown
        for (file in inputRoot.walk().filter(fileFilter.get())) {
            val out = if (inputRoot == outputRoot) file
            else outputRoot.resolve(inputRoot.relativize(file)).also {
                Files.createDirectories(file.parent)
            }
            val result = FileCutter.process(file, this)
            if (result == null) {
                matching += file to out
                continue
            }
            processed += out to result
        }
        matching.filter { it.first != it.second }.forEach { (inFile, outFile) ->
            Files.createDirectories(outFile.parent)
            inFile.copyTo(outFile, true)
        }
        processed.forEach { (file, content) ->
            if (file.notExists()) Files.createDirectories(file.parent)
            file.writeText(content, StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
        return true
    }
}