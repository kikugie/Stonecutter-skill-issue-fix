package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.StonecutterProject
import dev.kikugie.stonecutter.util.filename
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import kotlin.io.path.notExists

/**
 * Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterSettings(private val settings: Settings) {
    private val projects: ProjectSetup.SetupContainer =
        settings.gradle.extensions.create("stonecutterProjects", ProjectSetup.SetupContainer::class.java)
    private var shared = SharedConfigBuilder.DEFAULT
    private val controller
        get() = if (kotlinController) KotlinController else GroovyController

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

    var kotlinController = false
    var centralScript = "build.gradle"
        set(value) {
            if (value.startsWith("stonecutter.gradle")) throw StonecutterGradleException(
                "Invalid buildscript name"
            ) {
                exampleClosure { kts ->
                    "${if (kts) "centralScript = $value" else "setCentralScript($value)"}\n// ..."
                }
            }
            field = value
        }

    /**
     * Sets the buildscript used by all subprojects.
     * Defaults to `build.gradle`.
     */
    @Deprecated(
        message = "This method is deprecated, use property setter instead",
        replaceWith = ReplaceWith("centralScript = file")
    )
    fun centralScript(file: String) {
        centralScript = file
    }

    /**
     * Enables Kotlin buildscripts for the controller.
     * - `stonecutter.gradle` -> `stonecutter.gradle.kts`
     *
     * @param value Whenever Kotlin should be used. Setting it to `false` won't do anything.
     */
    @Deprecated(
        message = "This method is deprecated, use property setter instead",
        replaceWith = ReplaceWith("kotlinController = value")
    )
    fun kotlinController(value: Boolean) {
        kotlinController = value
    }

    /**
     * Specifies version directories and initial active version.
     */
    fun shared(builder: Action<SharedConfigBuilder>) {
        shared = SharedConfigBuilder(shared, builder)
    }

    /**
     * Applies Stonecutter to a project, creating `stonecutter.gradle` and applying plugin to the buildscript.
     *
     * @param project one or more projects to be included. Use `rootProject` for standard mod setup.
     */
    fun create(project: ProjectDescriptor) {
        create(project) {
            if (versions.isEmpty()) throw StonecutterGradleException("No version have been specified") {
                exampleClosure {
                    """
                    // Example
                    shared {
                        versions("1.19.4", "1.20.1" /*, etc*/)
                        // optional for more control
                        vers("1.20.4-test", "1.20.4")
                    }
                    create(rootProject)
                    """.trimIndent()
                }
            }
        }
    }

    /**
     * Applies Stonecutter to a project, creating `stonecutter.gradle` and applying plugin to the buildscript.
     *
     * @param projects one or more projects to be included. Use `rootProject` for standard mod setup.
     */
    fun create(vararg projects: ProjectDescriptor) {
        projects.forEach(::create)
    }

    private fun create(project: ProjectDescriptor, action: Action<SharedConfigBuilder>) {
        val builder = SharedConfigBuilder(shared, action)

        val vcs = builder.vcsVersionImpl
        if (!projects.register(project.path, builder))
            throw StonecutterGradleException("Project ${project.path} is already registered")

        project.buildFileName = controller.filename
        val file = project.projectDir.resolve(controller.filename).toPath()
        if (file.notExists()) controller.createHeader(file, vcs.project)
        builder.versions.forEach { createProject(project, it) }
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

    private fun exampleClosure(contents: (Boolean) -> String): String {
        val path = settings.filename
        val kts = path.endsWith("kts")

        return buildString {
            append("// $path")
            append("${if (kts) "extensions.configure<StonecutterSettings>" else "stonecutter"} {")
            contents(kts).split('\n').forEach {
                append("    $it")
            }
            append("}")
        }
    }
}