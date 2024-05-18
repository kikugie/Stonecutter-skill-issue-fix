package dev.kikugie.stonecutter.gradle

import dev.kikugie.stitcher.process.access.Expression
import dev.kikugie.stonecutter.util.buildDirectory
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


open class StonecutterBuild(private val project: Project) {
    private val setup = project.parent?.let {
        project.gradle.extensions.getByType(StonecutterConfiguration.Container::class.java)[it]
    } ?: throw StonecutterGradleException(
        """Project ${project.path} must be a versioned project.
            This might've been caused by applying the plugin in standard mod. 
            Read https://github.com/kikugie/stonecutter-kt/wiki for an integration guide.
            """.trimMargin()
    )
    val current: StonecutterProject by lazy {
        setup.versions.first { it.project in project.name }.let {
            if (it.project == setup.current.project) it.asActive() else it
        }
    }
    val active get() = setup.current
    val versions get() = setup.versions

    internal val constants = mutableMapOf<String, Boolean>()
    internal val expressions = mutableListOf<Expression>()
    internal val swaps = mutableMapOf<String, String>()
    internal val filters = mutableListOf<(Path) -> Boolean>()

    fun swap(identifier: String, replacement: String) {
        swaps[identifier] = replacement
    }

    fun swap(identifier: String, replacement: () -> String) {
        swap(identifier, replacement())
    }

    fun swaps(vararg values: Pair<String, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    fun const(identifier: String, value: Boolean) {
        constants[identifier] = value
    }

    fun const(identifier: String, value: () -> Boolean) {
        const(identifier, value())
    }

    fun consts(vararg values: Pair<String, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    fun expression(expression: Expression) {
        expressions += expression
    }

    fun filter(criteria: (Path) -> Boolean) {
        filters += criteria
    }

    fun test(version: String, predicate: String): Boolean {
        return false
    }

    init {
        project.tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            if (project.parent == null)
                throw StonecutterGradleException("Chiseled task can't be registered for the root project. How did you manage to do it though?")

            toVersion.set(current)

            constants.set(this@StonecutterBuild.constants)
            expressions.set(this@StonecutterBuild.expressions)
            swaps.set(this@StonecutterBuild.swaps)
            filter.set { p -> if (filters.isEmpty()) true else filters.all { it(p) } }

            input.set(project.parent!!.file("./src").toPath())
            output.set(project.buildDirectory.toPath().resolve("chiseledSrc"))
        }

        project.afterEvaluate {
            copyFabricLoader(this)
            configureSources(this)
        }
    }

    private fun configureSources(project: Project) {
        try {
            val formatter: (SourceSet, String) -> Any = if (setup.anyChiseled(project.gradle.startParameter.taskNames))
                { source, type -> File(project.buildDirectory, "chiseledSrc/${source.name}/$type") }
            else if (current.isActive) {
                { source, type -> "../../src/${source.name}/$type" }
            } else return

            (project.property("sourceSets") as SourceSetContainer).forEach {
                it.java.srcDir(formatter(it, "java"))
                it.resources.srcDir(formatter(it, "resources"))
                it.java.srcDir(formatter(it, "kotlin"))
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