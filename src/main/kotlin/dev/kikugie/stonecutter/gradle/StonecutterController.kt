package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.cutter.StonecutterTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

/**
 * Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.
 * @see StonecutterBuild
 * @see StonecutterSettings
 */
@Suppress("unused")
open class StonecutterController(project: Project) {
    private val setup: ProjectSetup = project.gradle.extensions.getByType<ProjectSetup.SetupContainer>()[project]
        ?: throw GradleException("Project ${project.path} is not registered")
    private val controller = project.controller
        ?: throw GradleException("Project ${project.path} is not a Stonecutter controller")

    /**
     * All registered subprojects.
     */
    val versions: List<SubProject> = setup.versions

    /**
     * Chiseled task type reference.
     */
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java


    init {
        setup.versions.forEach { project.project(it.project).pluginManager.apply(StonecutterPlugin::class.java) }
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
    infix fun active(str: String) {
        setup.current = setup.versions.find { it.project == str }
            ?: throw GradleException("[Stonecutter] Project $str is not registered")
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
    infix fun registerChiseled(provider: TaskProvider<*>) {
        setup.register(provider.name)
    }

    private fun setupProject(root: Project) {
        setup.versions.forEach { ver ->
            val project = root.project(ver.project)
            val projectVersion = project.extensions.getByType<StonecutterBuild>().current

            root.tasks.create(
                "Set active project to ${projectVersion.project}",
                StonecutterTask::class.java
            ).apply {
                group = "stonecutter"
                debug.set(setup.debug)
                expressions.set(project.extensions.getByType<StonecutterBuild>().expressions)

                toVersion.set(projectVersion)
                input.set(root.file("./src").toPath())
                output.set(input.get())

                doLast {
                    controller.updateHeader(this.project.buildFile.toPath(), projectVersion.project)
                }
            }
        }
    }
}