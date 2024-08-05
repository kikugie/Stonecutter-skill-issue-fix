@file:Suppress("unused")

package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.configuration.StonecutterConfiguration
import dev.kikugie.stonecutter.configuration.StonecutterControllerModelBuilder
import dev.kikugie.stonecutter.configuration.StonecutterGlobalParameters
import dev.kikugie.stonecutter.configuration.StonecutterUtility
import dev.kikugie.stonecutter.process.StonecutterTask
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path
import javax.inject.Inject

/**
 * Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterController @Inject internal constructor(registry: ToolingModelBuilderRegistry, root: Project) : StonecutterConfiguration, StonecutterUtility, StonecutterGlobalParameters {
    private val manager: ControllerManager = root.controller()
        ?: throw StonecutterGradleException("Project ${root.path} is not a Stonecutter controller. What did you even do to get this error?")
    internal val setup: StonecutterSetup =
        root.gradle.extensions.getByType(StonecutterSetup.Container::class.java)[root]
            ?: throw StonecutterGradleException("Project ${root.path} is not registered. This might've been caused by removing a project while its active")
    private var globalDebug: Boolean? = null
    private val delegatedActions: MutableList<StonecutterBuild.() -> Unit> = mutableListOf()

    /**
     * Project assigned by `stonecutter.active "..."`.
     */
    lateinit var current: StonecutterProject
        private set

    /**
     * All versions registered by [StonecutterSettings].
     */
    val versions: List<StonecutterProject> get() = setup.versions

    /**
     * All projects registered by [StonecutterSettings].
     */
    val projects: List<Project> = setup.versions.map { root.project(it.project) }

    /**
     * Chiseled task type accessor to avoid imports.
     */
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java

    @ApiStatus.Experimental
    override var automaticPlatformConstants: Boolean = false

    init {
        println("Running Stonecutter 0.5-alpha.3")
        registry.register(StonecutterControllerModelBuilder())
        versions.forEach { root.project(it.project).pluginManager.apply(StonecutterPlugin::class.java) }
        root.tasks.create("chiseledStonecutter") {
            setup.versions.forEach { dependsOn("${it.project}:setupChiseledBuild") }
        }
        root.afterEvaluate { setupProject(this) }
    }

    /**
     * Assigns the active version to this project.
     *
     * **Do not call on your own.**
     *
     * @param str Project name
     */
    infix fun active(str: String) {
        val selected = setup.versions.find { it.project == str }?.asActive()
            ?: throw GradleException("[Stonecutter] Project $str is not registered")
        setup.current = selected
        current = selected
    }

    /**
     * Registers a [ChiseledTask] that delegates to the created task.
     *
     * @param provider Delegate task provider
     */
    infix fun registerChiseled(provider: TaskProvider<*>) {
        setup.register(provider.name)
    }

    /**
     * Allows accessing [StonecutterBuild] in the controller to organize the configuration.
     *
     * @param action Versioned configuration action
     */
    infix fun configureEach(action: Action<StonecutterBuild>) = forEachProject { action.execute(this) }
    override fun swap(identifier: String, replacement: String) = forEachProject { swap(identifier, replacement) }
    override fun const(identifier: String, value: Boolean) = forEachProject { const(identifier, value) }
    override fun dependency(identifier: String, version: String) = forEachProject { dependency(identifier, version) }
    override fun exclude(path: Path) = forEachProject { exclude(path) }
    override fun exclude(path: String) = forEachProject { exclude(path) }

    override var debug: Boolean
        get() = globalDebug ?: false
        set(value) {
            globalDebug = value
            forEachProject { debug = value }
        }

    private fun setupProject(root: Project) {
        val vcsProject = root.project(setup.vcsVersion.project)
        val vcs = setup.vcsVersion
        createStonecutterTask("Reset active project", root, vcsProject, vcs) {
            "Sets active version to ${vcs.project}. Run this before making a commit."
        }
        val active = setup.current
        createStonecutterTask("Refresh active project", root, root.project(active.project), active) {
            "Runs the comment processor on the active version. Useful for fixing comments in wrong states."
        }
        val projects = setup.versions.associateWith { root.project(it.project) }

        projects.forEach { (ver, project) ->
            val build = project.extensions.getByType<StonecutterBuild>()
            createStonecutterTask("Set active project to ${ver.project}", root, project, ver) {
                "Sets the active project to ${ver.project}, processing all versioned comments."
            }
            for (it in delegatedActions) it(build)
        }

        if (automaticPlatformConstants) configurePlatforms(projects.values)
    }

    private fun forEachProject(action: StonecutterBuild.() -> Unit) {
        delegatedActions += action
    }

    private fun configurePlatforms(projects: Iterable<Project>) {
        val key = "loom.platform"
        val platforms = mutableSetOf<String>()
        for (it in projects) it.findProperty(key)?.run {
            platforms += this.toString()
        }
        if (platforms.isEmpty()) return
        for (it in projects) it.findProperty(key)?.run {
            it.extensions.getByType<StonecutterBuild>().consts(this.toString(), platforms)
        }
    }

    private inline fun createStonecutterTask(
        name: String,
        root: Project,
        subproject: Project,
        version: StonecutterProject,
        crossinline desc: () -> String,
    ) {
        root.tasks.create<StonecutterTask>(name) {
            group = "stonecutter"
            description = desc()

            toVersion.set(version)
            fromVersion.set(setup.current)

            val build = subproject.extensions.getByType<StonecutterBuild>()
            data.set(build.data)

            input.set(root.file("./src").toPath())
            output.set(input.get())

            doLast {
                manager.updateHeader(this.project.buildFile.toPath(), version.project)
            }
        }
    }
}