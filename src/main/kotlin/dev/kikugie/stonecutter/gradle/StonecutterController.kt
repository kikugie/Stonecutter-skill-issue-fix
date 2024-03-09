package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.cutter.StonecutterTask
import dev.kikugie.stonecutter.metadata.StonecutterProject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path
import kotlin.properties.Delegates

/**
 * Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.
 * @see StonecutterBuild
 * @see StonecutterSettings
 */
@Suppress("unused")
open class StonecutterController(project: Project) {
    private val controller = project.controller
        ?: throw StonecutterGradleException("Project ${project.path} is not a Stonecutter controller. What did you even do to get this error?")
    private val setup: ProjectSetup = project.gradle.extensions.getByType<ProjectSetup.SetupContainer>()[project]
        ?: throw StonecutterGradleException("Project ${project.path} is not registered. This might've been caused by removing a project while its active") {
            val kts = controller.filename.endsWith("kts")
            """
                // ${controller.filename}
                // We politely ignore the full uppercase warning to not edit this and do it anyway.
                // After changing it run the `Refresh active project` task.
                stonecutter${if (kts) " " else "."}active "<a valid project>" /* [SC] DO NOT EDIT */
            """.trimIndent()
        }
    var debug: Boolean by Delegates.observable(false) { _, _, new ->
        setup.debug = new
    }

    /**
     * All registered subprojects.
     */
    val versions: List<StonecutterProject> = setup.versions

    /**
     * Chiseled task type reference.
     */
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java


    init {
        setup.versions.forEach { project.project(it.project).pluginManager.apply(StonecutterPlugin::class.java) }
        project.tasks.create("chiseledStonecutter") {
            setup.versions.forEach { dependsOn("${it.project}:setupChiseledBuild") }
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
     * Enables debug functionality. Currently, adds `true` and `false` expressions to the processor.
     *
     * @param value debug state
     */
    @Deprecated(
        message = "This method is deprecated, use property setter instead",
        replaceWith = ReplaceWith("debug = value")
    )
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

    /**
     * Adds a filter function to the list of file filters in the setup configuration.
     *
     * @param func The filter function that takes a Path object and returns a Boolean indicating whether the file should be included or not.
     */
    fun filter(func: (Path) -> Boolean) {
        setup.fileFilters.add(func)
    }

    private fun setupProject(root: Project) {
        val vcsProject = root.project(setup.vcs.project)
        val vcs = vcsProject.extensions.getByType<StonecutterBuild>().current
        root.tasks.create(
            "Reset active project", StonecutterTask::class.java
        ).applyConfig(root, vcsProject, vcs)
        root.tasks.create(
            "Refresh active project", StonecutterTask::class.java
        ).applyConfig(root, root.project(vcs.project), vcs)
        setup.versions.forEach { ver ->
            val project = root.project(ver.project)
            val version = project.extensions.getByType<StonecutterBuild>().current
            root.tasks.create(
                "Set active project to ${version.project}", StonecutterTask::class.java
            ).applyConfig(root, project, version)
        }
    }

    private fun StonecutterTask.applyConfig(root: Project, subproject: Project, version: StonecutterProject) {
        group = "stonecutter"
        debug.set(setup.debug)
        expressions.set(subproject.extensions.getByType<StonecutterBuild>().expressions)

        toVersion.set(version)
        input.set(root.file("./src").toPath())
        output.set(input.get())

        if (setup.fileFilters.isNotEmpty())
            fileFilter.set { p -> setup.fileFilters.all { it(p) } }

        doLast {
            controller.updateHeader(this.project.buildFile.toPath(), version.project)
        }
    }
}