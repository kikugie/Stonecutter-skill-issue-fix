package dev.kikugie.stonecutter

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.stitcher.lexer.IdentifierRecognizer.Companion.allowed
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

/**
 * Stonecutter plugin applied to the versioned build file.
 *
 * @property project The effective Gradle project
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
@OptIn(ExperimentalPathApi::class)
open class StonecutterBuild internal constructor(val project: Project) {
    private val setup = project.parent?.let {
        project.gradle.extensions.getByType(StonecutterConfiguration.Container::class.java)[it]
    } ?: throw StonecutterGradleException(
        """Project ${project.path} must be a versioned project.
            This might've been caused by applying the plugin in standard mod. 
            Read https://github.com/kikugie/stonecutter-kt/wiki for an integration guide.
            """.trimMargin()
    )

    /**
     * Metadata of the currently processed version.
     */
    val current: StonecutterProject by lazy {
        setup.versions.first { it.project == project.name }.let {
            if (it.project == setup.current.project) it.asActive() else it
        }
    }

    /**
     * The currently active version. Global for all instances of the build file.
     */
    val active get() = setup.current

    /**
     * All available versions.
     */
    val versions get() = setup.versions

    /**
     * Creates a swap id.
     *
     * @param identifier swap name
     * @param replacement replacement string
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: String, replacement: String) {
        swaps[validate(identifier)] = replacement
    }

    /**
     * Creates a swap id.
     *
     * @param identifier swap name
     * @param replacement replacement string provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: String, replacement: () -> String) {
        swap(validate(identifier), replacement())
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(vararg values: Pair<String, String>) {
        values.forEach { (id, str) -> swap(validate(id), str) }
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(values: Iterable<Pair<String, String>>) {
        values.forEach { (id, str) -> swap(validate(id), str) }
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier constant name
     * @param value boolean value
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: String, value: Boolean) {
        constants[validate(identifier)] = value
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier constant name
     * @param value boolean value provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: String, value: () -> Boolean) {
        const(validate(identifier), value())
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(vararg values: Pair<String, Boolean>) {
        values.forEach { (id, str) -> const(validate(id), str) }
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(values: Iterable<Pair<String, Boolean>>) {
        values.forEach { (id, str) -> const(validate(id), str) }
    }

    /**
     * Adds a dependency to the semver checks.
     *
     * @param identifier dependency name
     * @param version dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: String, version: String) {
        dependencies[validate(identifier)] = SemanticVersionParser.parse(version)
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependencies(vararg values: Pair<String, String>) {
        values.forEach { (id, ver) -> dependencies[validate(id)] = SemanticVersionParser.parse(ver) }
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependencies(values: Iterable<Pair<String, String>>) {
        values.forEach { (id, ver) -> dependencies[validate(id)] = SemanticVersionParser.parse(ver) }
    }

    fun whitelist(criteria: (Path) -> Boolean) {
        filters += criteria
    }

    fun blacklist(criteria: (Path) -> Boolean) {
        filters += { !criteria(it) }
    }

    /**
     * Parses both parameters as semantic versions and compares them.
     *
     * @param left version on the left side of the comparison
     * @param right version on the right side of the comparison
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    fun compare(left: String, right: String) =
        SemanticVersionParser.parse(left).compareTo(SemanticVersionParser.parse(right))

    internal val constants = mutableMapOf<String, Boolean>()
    internal val swaps = mutableMapOf<String, String>()
    internal val filters = mutableListOf<(Path) -> Boolean>()
    internal val dependencies = mutableMapOf<String, SemanticVersion>()

    private fun validate(id: String): String {
        require(id.all(::allowed)) { "Invalid identifier: $id" }
        return id
    }

    init {
        project.tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            if (project.parent == null)
                throw StonecutterGradleException("Chiseled task can't be registered for the root project. How did you manage to do it though?")

            toVersion.set(current)
            fromVersion.set(active)

            constants.set(this@StonecutterBuild.constants)
            swaps.set(this@StonecutterBuild.swaps)
            dependencies.set(this@StonecutterBuild.dependencies)
            filter.set { p -> if (filters.isEmpty()) true else filters.all { it(p) } }

            input.set(project.parent!!.file("./src").toPath())
            val out = project.buildDirectory.toPath().resolve("chiseledSrc")
            if (out.exists()) out.deleteRecursively()
            output.set(out)
        }

        project.afterEvaluate {
            configureSources(this)
        }
    }

    private fun configureSources(project: Project) {
        try {
            val format: (Path) -> Any = if (setup.anyChiseled(project.gradle.startParameter.taskNames))
                { src -> File(project.buildDirectory, "chiseledSrc/$src") }
            else if (current.isActive) {
                { src -> "../../src/$src" }
            } else return

            val parentDir = project.parent!!.projectDir.resolve("src").toPath()
            val thisDir = project.projectDir.resolve("src").toPath()

            fun applyChiseled(from: SourceDirectorySet, to: SourceDirectorySet = from) {
                from.sourceDirectories.mapNotNull {
                    val relative = thisDir.relativize(it.toPath())
                    if (relative.startsWith(".."))
                        return@mapNotNull if (current.isActive) null
                        else parentDir.relativize(it.toPath())
                    else relative
                }.forEach {
                    to.srcDir(format(it))
                }
            }

            for (it in project.property("sourceSets") as SourceSetContainer) {
                applyChiseled(it.allJava, it.java)
                applyChiseled(it.resources)
            }
        } catch (ignored: MissingPropertyException) {
        }
    }
}