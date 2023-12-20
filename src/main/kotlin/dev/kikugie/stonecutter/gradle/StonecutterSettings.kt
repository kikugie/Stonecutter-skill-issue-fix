package dev.kikugie.stonecutter.gradle

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.writeText

/**
 * Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.
 *
 * Example setup:
 * ```gradle
 *  multidev {
 *      shared {
 *          versions "1.20.2", "1.19.4" // These don't have to be exactly named as Minecraft versions.
 *          vcs "1.19.4" // Optional, uses first of versions by default.
 *      }
 *      build "common.gradle.kts" // Optional if you want custom buildscript. Default is build.gradle.
 *      create rootProject
 *  }
 * ```
 * @see ProjectBuilder
 */
class StonecutterSettings(private val settings: Settings) {
    private val projects: ProjectSetup.SetupContainer =
        settings.gradle.extensions.create("stonecutterProjects", ProjectSetup.SetupContainer::class.java)

    private var shared = ProjectBuilder.DEFAULT
    private var build = "build.gradle"

    fun shared(builder: Action<ProjectBuilder>) {
        shared = ProjectBuilder(shared, builder)
    }

    fun build(file: String) {
        build = file
    }

    fun create(project: ProjectDescriptor, action: Action<ProjectBuilder>) {
        val builder = ProjectBuilder(shared, action)
        val versions = builder.versions

        if (versions.isEmpty())
            throw GradleException("[Stonecutter] Must have at least one version")
        val vcs = builder.vcsVersion ?: versions.first()
        if (!projects.register(project.path, builder))
            throw IllegalArgumentException("[Stonecutter] Project ${project.path} is already registered")

        try {
            project.buildFileName = "stonecutter.gradle"
            val file = Path("stonecutter.gradle")
            if (file.notExists()) file.writeText(
                createHeader(vcs),
                Charsets.UTF_8,
                StandardOpenOption.CREATE
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        builder.versions.forEach { createProject(project, it) }
    }

    private fun createHeader(vcs: ProjectName) = """
                plugins.apply "dev.kikugie.stonecutter"
                stonecutter.active "$vcs"
                stonecutter.vcs "$vcs"
                //-------- !DO NOT EDIT ABOVE THIS LINE! --------\\
            """.trimIndent()

    private fun createProject(root: ProjectDescriptor, version: ProjectName) {
        val path = root.path.let { "${it.trimEnd(':')}:$version" }
        settings.include(path)
        val project = settings.project(path)

        val versionDir = File("${project.projectDir}/versions/$version")
        versionDir.mkdirs()

        // TODO: Regex tokens file
        project.projectDir = versionDir
        project.name = version
        project.buildFileName = "../../$build"
    }
}