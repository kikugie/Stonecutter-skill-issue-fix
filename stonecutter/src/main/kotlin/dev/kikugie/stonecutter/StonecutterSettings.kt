package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.configuration.StonecutterInitialization
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import kotlin.io.path.notExists

/**
 * Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(private val settings: Settings) : StonecutterInitialization {
    private val projects = settings.gradle.extensions
        .create("stonecutterProjects", StonecutterSetup.Container::class.java)
    private val controller get() = if (kotlinController) KotlinController else GroovyController
    private lateinit var shared: StonecutterSetupBuilder

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

    override fun shared(builder: Action<StonecutterSetupBuilder>) {
        shared = StonecutterSetupBuilder(builder)
    }

    override fun create(project: String) = create(project, shared)

    override fun create(project: String, setup: StonecutterSetupBuilder) = with(project.removePrefix(":")) {
        settings.include(this)
        create(settings.project(":$this"), setup)
    }

    override fun create(project: ProjectDescriptor) = create(project, shared)

    override fun create(project: ProjectDescriptor, setup: StonecutterSetupBuilder) {
        val vcs = setup.vcsProject
        if (!projects.register(project.path, setup))
            throw StonecutterGradleException("Project ${project.path} is already registered")

        project.buildFileName = controller.filename
        val file = project.projectDir.resolve(controller.filename).toPath()
        if (file.notExists()) controller.createHeader(file, vcs.project)
        setup.versions.forEach { createProject(project, it) }
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