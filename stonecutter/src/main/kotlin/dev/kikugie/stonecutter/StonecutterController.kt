@file:Suppress("unused")

package dev.kikugie.stonecutter

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.encodeToStream
import dev.kikugie.stonecutter.data.StonecutterData
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

/**
 * Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterController internal constructor(private val root: Project) : StonecutterConfiguration {
    private val controller: ControllerManager = root.controller()
        ?: throw StonecutterGradleException("Project ${root.path} is not a Stonecutter controller. What did you even do to get this error?")
    private val setup: StonecutterSetup =
        root.gradle.extensions.getByType(StonecutterSetup.Container::class.java)[root]
            ?: throw StonecutterGradleException("Project ${root.path} is not registered. This might've been caused by removing a project while its active")
    private var globalDebug: Boolean? = null

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

    init {
        println("Running Stonecutter 0.4.2")
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
        setup.versions.forEach { ver ->
            val project = root.project(ver.project)
            createStonecutterTask("Set active project to ${ver.project}", root, project, ver) {
                "Sets the active project to ${ver.project}, processing all versioned comments."
            }
        }
    }

    private inline fun forEachProject(action: StonecutterBuild.() -> Unit) {
        projects.forEach { it.extensions.getByType<StonecutterBuild>().apply(action) }
    }

    private inline fun createStonecutterTask(name: String, root: Project, subproject: Project, version: StonecutterProject, crossinline desc: () -> String) {
        root.tasks.create<StonecutterTask>(name) {
            group = "stonecutter"
            description = desc()

            toVersion.set(version)
            fromVersion.set(setup.current)

            val build = subproject.extensions.getByType<StonecutterBuild>()
            debug.set(build.debug)
            constants.set(build.constants)
            swaps.set(build.swaps)
            dependencies.set(build.dependencies)
            filter.set(FileFilter(build.excludedExtensions, build.excludedPaths))

            input.set(root.file("./src").toPath())
            output.set(input.get())

            doLast {
                controller.updateHeader(this.project.buildFile.toPath(), version.project)
            }
        }
    }

    private fun saveModel() {
        val path = root.buildDirectory.resolve("stonecutter-cache/model.yml").toPath()
        runCatching {
            path.parent.createDirectories()
            path.outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
                Yaml.default.encodeToStream(setup, it)
            }
        }
    }
}