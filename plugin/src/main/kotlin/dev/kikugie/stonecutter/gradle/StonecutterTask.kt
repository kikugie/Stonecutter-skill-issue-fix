package dev.kikugie.stonecutter.gradle

import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.Transformer
import dev.kikugie.stitcher.process.access.Expression
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.recognizer.StandardSingleLine
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

@Suppress("LeakingThis")
@OptIn(ExperimentalPathApi::class)
abstract class StonecutterTask : DefaultTask() {
    @get:Input
    abstract val input: Property<Path>

    @get:Input
    abstract val output: Property<Path>

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
    internal lateinit var expressionsImpl: List<Expression>

    @TaskAction
    fun run() {
        if (!input.isPresent || !output.isPresent || !toVersion.isPresent)
            throw IllegalArgumentException("[Stonecutter] StonecutterTask is not fully initialized")
        expressionsImpl = buildList {
            val checker = versionChecker.get()(project)
            val target = checker.parseVersion(toVersion.get().version)

            addAll(expressions.get())
            add(McVersionExpression(target, checker))
        }
        transform(input.get(), output.get(), filter.get())
    }

    init {
        versionChecker.convention { FabricVersionChecker.create(it) }
    }

    private fun transform(input: Path, output: Path, filter: (Path) -> Boolean) {
        val inPlace = input == output
        val skipped = mutableListOf<Path>()
        val processed = mutableListOf<Pair<Path, String>>()

        for (file in input.walk()) {
            val filtered = filter(file)

            if (!filtered) {
                if (!inPlace) skipped.add(file)
                continue
            }
            val original = file.readText(StandardCharsets.ISO_8859_1)
            val transformed = process(file)
            if (original == transformed) skipped.add(file)
            else processed.add(file to transformed)
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

    private fun process(file: Path): String = try {
        val recognizers = listOf(StandardMultiLine, StandardSingleLine)
        val scope = file.reader(StandardCharsets.ISO_8859_1).scan(recognizers).lex().parse()
        val transformer = Transformer.create(
            scope,
            recognizers,
            constants.get(),
            expressionsImpl,
            swaps.get()
        )
        transformer.process()
        Assembler.visitScope(scope)
    } catch (e: Exception) {
        throw RuntimeException("Failed to process $file").apply {
            initCause(e)
        }
    }
}