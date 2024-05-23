package dev.kikugie.stonecutter.gradle

import dev.kikugie.stitcher.process.access.Expression
import dev.kikugie.stonecutter.metadata.Semver
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

/**
 * Stonecutter plugin applied to the versioned build file.
 *
 * @property project the effective Gradle project
 */
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
        setup.versions.first { it.project in project.name }.let {
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

    internal val constants = mutableMapOf<String, Boolean>()
    internal val expressions = mutableListOf<Expression>()
    internal val swaps = mutableMapOf<String, String>()
    internal val filters = mutableListOf<(Path) -> Boolean>()
    internal val dependencies = mutableMapOf<String, Semver>()

    /**
     * Creates a swap id.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param identifier id identifier
     * @param replacement replacement string
     */
    fun swap(identifier: String, replacement: String) {
        swaps[identifier] = replacement
    }

    /**
     * Creates a swap id.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param identifier id identifier
     * @param replacement replacement string provider
     */
    fun swap(identifier: String, replacement: () -> String) {
        swap(identifier, replacement())
    }

    /**
     * Creates a swap id.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param values entries of tokens to replacements
     */
    fun swaps(vararg values: Pair<String, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param identifier id identifier
     * @param value boolean value
     */
    fun const(identifier: String, value: Boolean) {
        constants[identifier] = value
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param identifier id identifier
     * @param value boolean value provider
     */
    fun const(identifier: String, value: () -> Boolean) {
        const(identifier, value())
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param values entries of tokens to boolean values
     */
    fun consts(vararg values: Pair<String, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param values entries of tokens to boolean values
     */
    fun consts(values: Iterable<Pair<String, Boolean>>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Creates an expression that allows stonecutter to dynamically evaluate condition requirements.
     *
     * Refer to the wiki for a detailed guide.
     *
     * @param expression expression function
     */
    fun expression(expression: Expression) {
        expressions += expression
    }

    fun whitelist(criteria: (Path) -> Boolean) {
        filters += criteria
    }

    fun blacklist(criteria: (Path) -> Boolean) {
        filters += { !criteria(it) }
    }

    fun dependency(identifier: String, version: Semver) {
        dependencies[identifier] = version
    }

    fun dependencies(vararg values: Pair<String, Semver>) {
        values.forEach { (id, ver) -> dependencies[id] = ver }
    }

    fun dependencies(values: Iterable<Pair<String, Semver>>) {
        values.forEach { (id, ver) -> dependencies[id] = ver }
    }

    init {
        project.tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            if (project.parent == null)
                throw StonecutterGradleException("Chiseled task can't be registered for the root project. How did you manage to do it though?")

            toVersion.set(current)
            fromVersion.set(active)

            constants.set(this@StonecutterBuild.constants)
            expressions.set(this@StonecutterBuild.expressions)
            swaps.set(this@StonecutterBuild.swaps)
            dependencies.set(this@StonecutterBuild.dependencies)
            filter.set { p -> if (filters.isEmpty()) true else filters.all { it(p) } }

            input.set(project.parent!!.file("./src").toPath())
            val out = project.buildDirectory.toPath().resolve("chiseledSrc")
            if (out.exists()) out.deleteRecursively()
            output.set(out)
        }

        project.afterEvaluate {
            copyFabricLoader(this)
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

            fun applyChiseled(src: SourceDirectorySet) {
                src.sourceDirectories.mapNotNull {
                    val relative = thisDir.relativize(it.toPath())
                    if (relative.startsWith(".."))
                        return@mapNotNull if (current.isActive) null
                        else parentDir.relativize(it.toPath())
                    else relative
                }.forEach {
                    src.srcDir(format(it))
                }
            }

            for (it in project.property("sourceSets") as SourceSetContainer) {
                applyChiseled(it.allJava)
                applyChiseled(it.resources)
            }
        } catch (ignored: MissingPropertyException) {
        }
    }

    private fun copyFabricLoader(project: Project) {
        var loaderCopy = File(project.rootDir, ".gradle/stonecutter")
        loaderCopy.mkdirs()
        loaderCopy = File(loaderCopy, "fabric-loader.jar")
        // Me when one line. Have a nice day
        if (!loaderCopy.exists()) loaderSearch@ for (configuration in project.configurations) for (dependency in configuration.dependencies) if ("net.fabricmc" == dependency.group && "fabric-loader" == dependency.name) for (file in configuration.files) if (file.getName()
                .startsWith("fabric-loader")
        )
            try {
                Files.copy(file.toPath(), loaderCopy.toPath())
                break@loaderSearch
            } catch (ignored: Exception) {
            }
    }
}