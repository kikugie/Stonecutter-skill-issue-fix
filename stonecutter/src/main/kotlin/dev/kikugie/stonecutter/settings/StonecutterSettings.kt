package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.manager.GroovyController
import dev.kikugie.stonecutter.controller.manager.KotlinController
import dev.kikugie.stonecutter.data.container.ProjectParameterContainer
import dev.kikugie.stonecutter.data.container.ProjectTreeContainer
import dev.kikugie.stonecutter.settings.builder.TreeBuilder
import dev.kikugie.stonecutter.data.container.TreeBuilderContainer
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Configures versions used by Stonecutter and creates the corresponding Gradle projects.
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#settings-settings-gradle-kts">Wiki page</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(settings: Settings) : SettingsConfiguration(settings), StonecutterUtility {
    private val container: TreeBuilderContainer
    private val controller get() = if (kotlinController) KotlinController else GroovyController

    /**
     * Enables Kotlin buildscripts for the controller.
     * - `stonecutter.gradle` -> `stonecutter.gradle.kts`
     */
    var kotlinController: Boolean = false

    /**Buildscript used by all subprojects. Defaults to `build.gradle`.*/
    var centralScript: String = "build.gradle"
        set(value) {
            require(!value.startsWith("stonecutter.gradle")) {
                "Build script must not override the controller"
            }
            field = value
        }

    init {
        with(settings.gradle.extensions) {
            container = create<TreeBuilderContainer>("stonecutterTreeBuilders")
            create<ProjectTreeContainer>("stonecutterProjectTrees")
            create<ProjectParameterContainer>("stonecutterProjectParameters")
        }
    }

    override fun create(project: ProjectDescriptor, setup: TreeBuilder) {
        require(container.register(project.path, setup)) {
            "Project ${project.path} is already registered"
        }

        project.buildFileName = controller.filename
        with(project.projectDir.resolve(controller.filename).toPath()) {
            if (notExists()) controller.createHeader(this, setup.vcsVersion!!)
        }

        setup.nodes.forEach { (name, branch) ->
            createBranch(name, project, setup, branch)
        }
    }

    private fun createBranch(
        name: Identifier,
        root: ProjectDescriptor,
        setup: TreeBuilder,
        branch: Collection<StonecutterProject>
    ) {
        require(branch.isNotEmpty()) { "Registered branch $name has no nodes" }
        val project = if (name.isEmpty()) root else "${root.path}:$name".project()
        project.projectDir.toPath().createDirectories()
        project.buildFileName = controller.filename

        val buildscript = runCatching { setup.branches[name]!!.buildscript }.getOrElse { centralScript }
        branch.forEach { createProject(project, it, buildscript) }
    }

    private fun createProject(
        root: ProjectDescriptor,
        version: StonecutterProject,
        buildscript: String
    ) {
        val project = "${root.path}:${version.project}".project()
        val versionDir = File("${root.projectDir}/versions/${version.project}")
        versionDir.mkdirs()

        project.projectDir = versionDir
        project.name = version.project
        project.buildFileName = "../../$buildscript"
    }
}