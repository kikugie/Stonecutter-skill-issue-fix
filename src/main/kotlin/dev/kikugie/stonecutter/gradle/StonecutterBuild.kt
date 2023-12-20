package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.cutter.StonecutterTask
import dev.kikugie.stonecutter.processor.Expression
import groovy.lang.MissingPropertyException
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.nio.file.Files
import kotlin.io.path.exists

/**
 * Provides versioned functionality in the buildscript.
 */
class StonecutterBuild(internal val project: Project) {
    internal val setup: ProjectSetup = project.gradle.extensions.getByType<ProjectSetup.SetupContainer>()[project.parent
        ?: throw GradleException("[Stonecutter] Project ${project.path} must be a versioned project")
    ] ?: throw GradleException("[Stonecutter] Project ${project.path} is not registered in Stonecutter")
    private val version: ProjectVersion = ProjectVersion(this, project.name)

    val current = version
    val active = setup.current
    val versions = setup.versions
    internal val expressions = mutableListOf<Expression>()

    fun expression(expr: Expression) {
        expressions += expr
    }

    init {
        project.tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            if (project.parent == null)
                throw IllegalStateException("[Stonecutter] Chiseled task can't be registered for the root project")

            fromVersion.set(project.parent!!.project(setup.current).extensions.getByType<StonecutterBuild>().current)
            toVersion.set(version)

            input.set(project.parent!!.file("./src").toPath())
            output.set(project.buildDir.toPath().resolve("chiseledSrc"))
        }

        project.afterEvaluate {
            copyFabricLoader(this)
            configureSources(this)
        }
    }

    private fun configureSources(project: Project) {
        try {
            if (setup.anyChiseled(project.gradle.startParameter.taskNames)) {
                val template: (SourceSet, String) -> File = { source, type -> File("chiseledSrc/${source.name}/$type") }
                (project.property("sourceSets") as SourceSetContainer).forEach {
                    it.java.srcDir(template(it, "java"))
                    it.java.srcDir(template(it, "kotlin"))
                    it.resources.srcDir(template(it, "resources"))
                }
            } else if (version.active) {
                val template: (SourceSet, String) -> String = { source, type -> "../../${source.name}/$type" }
                (project.property("sourceSets") as SourceSetContainer).forEach {
                    it.java.srcDir(template(it, "java"))
                    it.java.srcDir(template(it, "kotlin"))
                    it.resources.srcDir(template(it, "resources"))
                }
            }
        } catch (ignored: MissingPropertyException) {
        }
    }

    private fun copyFabricLoader(project: Project) {
        var loaderCopy = project.rootDir.toPath().resolve(".gradle/stonecutter")
        Files.createDirectories(loaderCopy)
        loaderCopy = loaderCopy.resolve("fabric-loader.jar")
        if (!loaderCopy.exists()) search@ for (conf in project.configurations) deps@ for (dep in conf.dependencies) {
            if (dep.group != "net.fabricmc" || dep.name != "fabric-loader") continue@deps
            for (file in conf.files()) {
                if (!file.name.startsWith("fabric-loader")) continue
                try {
                    Files.copy(file.toPath(), loaderCopy)
                    break@search
                } catch (ignored: Exception) {
                }
            }
        }

    }
}