package dev.kikugie.stonecutter.cutter

import dev.kikugie.stonecutter.gradle.ProjectVersion
import dev.kikugie.stonecutter.processor.ConditionProcessor
import dev.kikugie.stonecutter.processor.Expression
import dev.kikugie.stonecutter.processor.McVersionExpression
import dev.kikugie.stonecutter.version.FabricVersionChecker
import dev.kikugie.stonecutter.version.VersionChecker
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

@Suppress("LeakingThis")
abstract class StonecutterTask : DefaultTask() {
    @get:Input
    abstract val input: Property<Path>

    @get:Input
    abstract val output: Property<Path>

    @get:Input
    abstract val fromVersion: Property<ProjectVersion>

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

    lateinit var processor: ConditionProcessor

    init {
        fileFilter.convention { true }
        versionChecker.convention { FabricVersionChecker.create(it) }
        debug.convention(false)
        expressions.convention(emptyList())
    }

    @TaskAction
    fun run() {
        if (!input.isPresent || !output.isPresent || !fromVersion.isPresent || !toVersion.isPresent)
            throw IllegalArgumentException("[Stonecutter] StonecutterTask is not fully initialized")
        processor = ConditionProcessor(collectExpressions())
        // TODO: Regex tokenizer

        transform(input.get(), input.get(), output.get())
    }

    private fun collectExpressions(): List<Expression> {
        val exprs = mutableListOf<Expression>()
        if (debug.get()) {
            exprs += ConditionProcessor.TRUE
            exprs += ConditionProcessor.FALSE
        }
        val checker = versionChecker.get()(project)
        val target = checker.parseVersion(toVersion.get().version)
        exprs += McVersionExpression(target, checker)
        exprs.addAll(expressions.get())
        return exprs
    }

    private fun transform(file: Path, inputRoot: Path, outputRoot: Path) {
        if (!file.exists()) return

        if (file.isDirectory())
            file.listDirectoryEntries().forEach { transform(it, inputRoot, outputRoot) }
        else if (fileFilter.get()(file)) {
            var output = file
            if (inputRoot != outputRoot) {
                output = outputRoot.resolve(inputRoot.relativize(output))
                Files.createDirectories(output.parent)
            }
            FileCutter(file, this).write(output)
        }
    }
}