package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.cutter.StonecutterTask
import dev.kikugie.stonecutter.metadata.StonecutterProject
import dev.kikugie.stonecutter.processor.Expression
import dev.kikugie.stonecutter.util.buildDirectory
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.nio.file.Files

/**
 * Provides versioned functionality in the buildscript.
 */
@Suppress("unused")
open class StonecutterBuild(internal val project: Project) {
    internal val setup: ProjectSetup = project.gradle.extensions.getByType<ProjectSetup.SetupContainer>()[project.parent
        ?: throw StonecutterGradleException(
            """Project ${project.path} must be a versioned project.
            This might've been caused by applying the plugin in standard mod. 
            Read https://github.com/kikugie/stonecutter-kt/wiki for an integration guide.
            """.trimMargin()
        )]!!

    /**
     * Version of this buildscript instance. (Unique for each subproject)
     */
    val current: StonecutterProject = setup.versions.find { it.project == project.name }!!.let {
        StonecutterProject(it.project, it.version, this)
    }

    /**
     * Current active version. (Global for all subprojects)
     */
    val active: StonecutterProject = setup.current.let { StonecutterProject(it.project, it.version, this) }

    /**
     * All registered subprojects.
     */
    val versions
        get() = setup.versions
    internal val expressions = mutableListOf<Expression>()

    /**
     * Create a custom expression for the comment processor.
     *
     * @param expr function that accepts an expression string and returns a boolean result, or `null` if expression doesn't match.
     */
    fun expression(expr: Expression) {
        expressions += expr
    }

    init {
        project.tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            if (project.parent == null)
                throw StonecutterGradleException("Chiseled task can't be registered for the root project. How did you manage to do it though?")

            toVersion.set(current)
            expressions.set(this@StonecutterBuild.expressions)
            debug.set(setup.debug)

            input.set(project.parent!!.file("./src").toPath())
            output.set(project.buildDirectory.toPath().resolve("chiseledSrc"))
        }

        project.afterEvaluate {
            copyFabricLoader(this)
            configureSources(this)
        }
    }

    private fun configureSources(project: Project) {
        try {
            val formatter: (SourceSet, String) -> Any = if (setup.anyChiseled(project.gradle.startParameter.taskNames))
                { source, type -> File(project.buildDirectory, "chiseledSrc/${source.name}/$type") }
            else if (current.isActive)
                { source, type -> "../../src/${source.name}/$type" }
            else return

            (project.property("sourceSets") as SourceSetContainer).forEach {
                it.java.srcDir(formatter(it, "java"))
                it.resources.srcDir(formatter(it, "resources"))
                it.java.srcDir(formatter(it, "kotlin"))
            }
        } catch (ignored: MissingPropertyException) {
        }
    }

    private fun copyFabricLoader(project: Project) {
        var loaderCopy = File(project.rootDir, ".gradle/stonecutter")
        loaderCopy.mkdirs()
        loaderCopy = File(loaderCopy, "fabric-loader.jar")
        // Me when one line. Have a nice day
        if (!loaderCopy.exists()) loaderSearch@ for (configuration in project.configurations) for (dependency in configuration.dependencies) if ("net.fabricmc" == dependency.group && "fabric-loader" == dependency.name) for (file in configuration.files) if (file.getName()
                .startsWith("fabric-loader")
        )
            try {
                Files.copy(file.toPath(), loaderCopy.toPath())
                break@loaderSearch
            } catch (ignored: Exception) {
            }
    }
}