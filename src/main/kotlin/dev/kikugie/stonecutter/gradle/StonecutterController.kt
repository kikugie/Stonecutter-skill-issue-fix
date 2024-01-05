package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.cutter.StonecutterTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.io.path.useLines
import kotlin.io.path.writeLines

/**
 * Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.
 * @see StonecutterBuild
 * @see StonecutterSettings
 */
@Suppress("unused")
open class StonecutterController(project: Project) {
    private val setup: ProjectSetup = project.gradle.extensions.getByType<ProjectSetup.SetupContainer>()[project]
        ?: throw GradleException("Project ${project.path} is not registered in Stonecutter")

    /**
     * All registered subprojects.
     */
    val versions: List<ProjectName> = setup.versions

    /**
     * Chiseled task type reference.
     */
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java


    init {
        setup.versions.forEach { project.project(it).pluginManager.apply(StonecutterPlugin::class.java) }
        project.tasks.create("chiseledStonecutter") {
            setup.versions.forEach { dependsOn("$it:setupChiseledBuild") }
        }
        project.afterEvaluate { setupProject(this) }
    }

    /**
     * Sets active Stonecutter version.
     *
     * DO NOT call manually.
     *
     * @param str project version
     */
    fun active(str: String) {
        setup.current = str
    }

    /**
     * Enables debug functionality. Currently adds `true` and `false` expressions to the processor.
     *
     * @param value debug state
     */
    fun debug(value: Boolean) {
        setup.debug = value
    }

    /**
     * Registers a chiseled task, which runs in parallel for all versions.
     *
     * @param provider task provider.
     */
    fun registerChiseled(provider: TaskProvider<*>) {
        setup.register(provider.name)
    }

    private fun setupProject(root: Project) {
        val current = root.project(setup.current).extensions.getByType<StonecutterBuild>().current
        setup.versions.forEach { name ->
            val project = root.project(name)
            val projectVersion = project.extensions.getByType<StonecutterBuild>().current

            root.tasks.create(
                "Set active version to ${projectVersion.version}",
                StonecutterTask::class.java
            ).apply {
                group = "stonecutter"
                debug.set(setup.debug)
                expressions.set(project.extensions.getByType<StonecutterBuild>().expressions)

                fromVersion.set(current)
                toVersion.set(projectVersion)
                input.set(root.file("./src").toPath())
                output.set(input.get())

                doLast { updateInitScript(this, projectVersion.version) }
            }
        }
    }

    private fun updateInitScript(task: Task, version: String) {
        val initFile = task.project.buildFile.toPath()
        val lines = initFile.readLines(StandardCharsets.ISO_8859_1).toMutableList()
        lines[1] = "stonecutter.active \"$version\""
        initFile.writeLines(lines, StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE)
    }
}