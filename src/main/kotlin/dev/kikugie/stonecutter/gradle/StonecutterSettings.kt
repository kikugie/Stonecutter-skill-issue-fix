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
 *
 * Example setup:
 * ```gradle
 *  stonecutter {
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
@Suppress("unused")
open class StonecutterSettings(private val settings: Settings) {
    private val projects: ProjectSetup.SetupContainer =
        settings.gradle.extensions.create("stonecutterProjects", ProjectSetup.SetupContainer::class.java)

    private var shared = ProjectBuilder.DEFAULT
    private var build = "build.gradle"

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

    fun shared(builder: Action<ProjectBuilder>) {
        shared = ProjectBuilder(shared, builder)
    }

    fun build(file: String) {
        build = file
    }

    fun create(vararg projects: ProjectDescriptor) {
        for (proj in projects) create(proj) {
            if (versions.isEmpty()) throw GradleException("[Stonecutter] To create a stonecutter project without a configuration element, make use of shared default values")
        }
    }

    fun create(project: ProjectDescriptor, action: Action<ProjectBuilder>) {
        val builder = ProjectBuilder(shared, action)
        val versions = builder.versions

        if (versions.isEmpty())
            throw GradleException("[Stonecutter] Must have at least one version")
        val vcs = builder.vcsVersion ?: versions.first()
        if (!projects.register(project.path, builder))
            throw IllegalArgumentException("[Stonecutter] Project ${project.path} is already registered")

        project.buildFileName = "stonecutter.gradle"
        val file = project.projectDir.resolve("stonecutter.gradle").toPath()
        if (file.notExists()) file.writeText(
            createHeader(vcs),
            Charsets.UTF_8,
            StandardOpenOption.CREATE
        )
        builder.versions.forEach { createProject(project, it) }
    }

    private fun createHeader(vcs: ProjectName) = """
                plugins.apply "dev.kikugie.stonecutter"
                stonecutter.active "$vcs"
                //-------- !DO NOT EDIT ABOVE THIS LINE! --------\\
            """.trimIndent()

    private fun createProject(root: ProjectDescriptor, version: ProjectName) {
        val path = root.path.let { "${it.trimEnd(':')}:$version" }
        settings.include(path)
        val project = settings.project(path)

        val versionDir = File("${root.projectDir}/versions/$version")
        versionDir.mkdirs()

        // TODO: Regex tokens file
        project.projectDir = versionDir
        project.name = version
        project.buildFileName = "../../$build"
    }
}