package dev.kikugie.stonecutter

import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import kotlin.io.path.notExists

/**
 * Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(private val settings: Settings) {
    private val projects = settings.gradle.extensions
        .create("stonecutterProjects", StonecutterConfiguration.Container::class.java)
    private val controller get() = if (kotlinController) KotlinController else GroovyController
    private lateinit var shared: StonecutterConfigurationBuilder

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
            if (value.startsWith("stonecutter.gradle"))
                throw StonecutterGradleException("Invalid buildscript name")
            field = value
        }

    /**
     * Configures the version structure for this project.
     *
     * @param builder Configuration scope
     */
    fun shared(builder: Action<StonecutterConfigurationBuilder>) {
        shared = StonecutterConfigurationBuilder(builder)
    }

    /**
     * Includes the provided project path and assigns the specified configuration to it.
     *
     * @param project Project path
     */
    fun create(project: String) {
        val actual = project.removePrefix(":")
        settings.include(actual)
        create(settings.project(":$actual"))
    }

    /**
     * Includes the provided project paths and assigns the specified configuration to them.
     *
     * @param projects Project paths
     */
    fun create(vararg projects: String) {
        projects.forEach(::create)
    }

    /**
     * Includes the provided project paths and assigns the specified configuration to them.
     *
     * @param projects Project paths
     */
    fun create(projects: Iterable<String>) {
        projects.forEach(::create)
    }

    /**
     * Assigns the specified configuration to projects.
     *
     * @param projects Project references
     */
    fun create(projects: Iterable<ProjectDescriptor>) {
        projects.forEach(::create)
    }

    /**
     * Assigns the specified configuration to projects.
     *
     * @param projects Project references
     */
    fun create(vararg projects: ProjectDescriptor) {
        projects.forEach(::create)
    }

    /**
     * Assigns the specified configuration to the project.
     *
     * @param project Project reference
     */
    fun create(project: ProjectDescriptor) {
        val vcs = shared.vcsProject
        if (!projects.register(project.path, shared))
            throw StonecutterGradleException("Project ${project.path} is already registered")

        project.buildFileName = controller.filename
        val file = project.projectDir.resolve(controller.filename).toPath()
        if (file.notExists()) controller.createHeader(file, vcs.project)
        shared.versions.forEach { createProject(project, it) }
    }

    private fun createProject(root: ProjectDescriptor, version: StonecutterProject) {
        val path = root.path.let { "${it.trimEnd(':')}:${version.project}" }
        settings.include(path)
        val project = settings.project(path)

        val versionDir = File("${root.projectDir}/versions/${version.project}")
        versionDir.mkdirs()

        project.projectDir = versionDir
        project.name = version.project
        project.buildFileName = "../../$centralScript"
    }
}