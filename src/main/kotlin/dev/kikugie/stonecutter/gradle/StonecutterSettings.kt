package dev.kikugie.stonecutter.gradle

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.notExists
import kotlin.io.path.writeText

/**
 * Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.
 */
@Suppress("unused")
open class StonecutterSettings(private val settings: Settings) {
    private val projects: ProjectSetup.SetupContainer =
        settings.gradle.extensions.create("stonecutterProjects", ProjectSetup.SetupContainer::class.java)

    private var shared = ProjectBuilder.DEFAULT
    private var useKotlin = false
    private var useKotlinBuild = false
    private var useKotlinController = false
    private val controller
        get() = if (useKotlin || useKotlinController) KotlinController else GroovyController
    private val buildFile
        get() = if (useKotlin || useKotlinBuild) "build.gradle.kts" else "build.gradle"

    init {
        try {
            var stonecutter = File(settings.rootDir, ".gradle/stonecutter")
            stonecutter.mkdirs()
            val thisJar = File(javaClass.getProtectionDomain().codeSource.location.toURI())
            stonecutter = File(stonecutter, thisJar.getName())
            if (!stonecutter.exists()) Files.copy(thisJar.toPath(), stonecutter.toPath())
        } catch (ignored: Exception) {
        }
    }

    /**
     * Specifies version directories and initial active version.
     *
     * @param builder version settings.
     */
    fun shared(builder: Action<ProjectBuilder>) {
        shared = ProjectBuilder(shared, builder)
    }

    fun useKotlin(value: Boolean) {
        useKotlin = value
    }

    fun useKotlinBuild(value: Boolean) {
        useKotlinBuild = value
    }

    fun useKotlinController(value: Boolean) {
        useKotlinController = value
    }

    /**
     * Applies Stonecutter to a project, creating `stonecutter.gradle` and applying plugin to the buildscript.
     *
     * @param projects one or more projects to be included. Use `rootProject` for standard mod setup.
     */
    fun create(vararg projects: ProjectDescriptor) {
        for (proj in projects) create(proj) {
            if (versions.isEmpty()) throw GradleException("[Stonecutter] To create a stonecutter project without a configuration element, make use of shared default values")
        }
    }

    private fun create(project: ProjectDescriptor, action: Action<ProjectBuilder>) {
        val builder = ProjectBuilder(shared, action)
        val versions = builder.versions

        if (versions.isEmpty())
            throw GradleException("[Stonecutter] Must have at least one version")
        val vcs = builder.vcsVersion
        if (!projects.register(project.path, builder))
            throw IllegalArgumentException("[Stonecutter] Project ${project.path} is already registered")

        project.buildFileName = controller.filename
        val file = project.projectDir.resolve(controller.filename).toPath()
        if (file.notExists()) controller.createHeader(file, vcs.project)
        builder.versions.forEach { createProject(project, it) }
    }

    private fun createProject(root: ProjectDescriptor, version: SubProject) {
        val path = root.path.let { "${it.trimEnd(':')}:${version.project}" }
        settings.include(path)
        val project = settings.project(path)

        val versionDir = File("${root.projectDir}/versions/${version.project}")
        versionDir.mkdirs()

        project.projectDir = versionDir
        project.name = version.project
        project.buildFileName = "../../$buildFile"
    }
}