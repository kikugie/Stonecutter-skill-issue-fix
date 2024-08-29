package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.GroovyController
import dev.kikugie.stonecutter.controller.KotlinController
import dev.kikugie.stonecutter.data.TreeBuilderContainer
import dev.kikugie.stonecutter.data.TreeContainer
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File
import kotlin.io.path.notExists

@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(private val settings: Settings) : SettingsConfiguration, StonecutterUtility {
    private val container: TreeBuilderContainer =
        settings.gradle.extensions.create<TreeBuilderContainer>("stonecutterTreeBuilders")
    private lateinit var shared: TreeBuilder

    private val controller get() = if (kotlinController) KotlinController else GroovyController

    /**
     * Enables Kotlin buildscripts for the controller.
     * - `stonecutter.gradle` -> `stonecutter.gradle.kts`
     */
    var kotlinController = false

    /**
     * Buildscript used by all subprojects.
     * Defaults to `build.gradle`.
     */
    var centralScript = "build.gradle"
        set(value) {
            require(!value.startsWith("stonecutter.gradle")) { "Build script must not override the controller" }
            field = value
        }

    init {
        settings.gradle.extensions.create<TreeContainer>("stonecutterProjectTrees")
    }

    override fun shared(action: Action<TreeBuilder>) {
        shared = TreeBuilder().also(action::execute)
    }

    override fun create(project: String) {
        create(get(project), shared)
    }

    override fun create(project: ProjectDescriptor) {
        settings.include(project.path)
        create(project, shared)
    }

    override fun create(project: String, action: Action<TreeBuilder>) {
        create(get(project), action)
    }

    override fun create(project: ProjectDescriptor, action: Action<TreeBuilder>) {
        create(project, TreeBuilder().also(action::execute))
    }

    private fun create(project: ProjectDescriptor, setup: TreeBuilder) {
        require(container.register(project.path, setup)) {
            "Project ${project.path} is already registered"
        }
        println("Created tree:\n$setup")

        project.buildFileName = controller.filename
        with(project.projectDir.resolve(controller.filename).toPath()) {
            if (notExists()) controller.createHeader(this, setup.vcsVersion!!)
        }

        setup.nodes.forEach { (name, branch) ->
            createBranch(name.sanitize(), project, setup, branch)
        }
    }

    private fun createBranch(
        name: ProjectName,
        project: ProjectDescriptor,
        setup: TreeBuilder,
        branch: Iterable<StonecutterProject>
    ) {
        val scope = if (name.isEmpty()) project else get("${project.path.sanitize()}:$name")
        val file = runCatching { setup.branches[name]!!.buildscript }.getOrNull() ?: centralScript
        branch.forEach { createProject(scope, it, file) }
    }

    private fun createProject(
        root: ProjectDescriptor,
        version: StonecutterProject,
        build: String
    ) {
        val project = get("${root.path.sanitize()}:${version.project}")
        val versionDir = File("${root.projectDir}/versions/${version.project}")
        versionDir.mkdirs()

        project.projectDir = versionDir
        project.name = version.project
        project.buildFileName = "../../$build"
    }

    private fun get(path: ProjectPath): ProjectDescriptor =
        with(path.sanitize()) {
            if (isEmpty()) settings.rootProject
            else {
                settings.include(this)
                settings.project(":$this")
            }
        }
}