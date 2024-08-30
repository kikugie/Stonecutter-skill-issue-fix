package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.StonecutterUtility
import dev.kikugie.stonecutter.controller.GroovyController
import dev.kikugie.stonecutter.controller.KotlinController
import dev.kikugie.stonecutter.data.TreeBuilderContainer
import dev.kikugie.stonecutter.data.TreeContainer
import dev.kikugie.stonecutter.sanitize
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.writeText

@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(settings: Settings) : SettingsConfiguration(settings), StonecutterUtility {
    private val container: TreeBuilderContainer =
        settings.gradle.extensions.create<TreeBuilderContainer>("stonecutterTreeBuilders")

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

    override fun create(project: ProjectDescriptor, setup: TreeBuilder) {
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
        scope.projectDir.toPath().createDirectories()
        with(scope.projectDir.resolve(file).toPath()) {
            if (notExists()) writeText("", Charsets.UTF_8, StandardOpenOption.CREATE_NEW)
        }
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
}