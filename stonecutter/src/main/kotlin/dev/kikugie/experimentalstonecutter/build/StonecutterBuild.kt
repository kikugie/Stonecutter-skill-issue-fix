package dev.kikugie.experimentalstonecutter.build

import dev.kikugie.experimentalstonecutter.settings.TreeContainer
import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.lexer.IdentifierRecognizer.Companion.allowed
import dev.kikugie.experimentalstonecutter.StonecutterProject
import dev.kikugie.experimentalstonecutter.StonecutterUtility
import dev.kikugie.stonecutter.configuration.buildDirectory
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterBuild(val project: Project) : BuildConfiguration, StonecutterUtility {
    internal val data = StonecutterData()
    internal val tree = requireNotNull(project.parent) { "Project ${project.path} must be a versioned project." }
        .run { gradle.extensions.getByType<TreeContainer>()[this]!! }

    /**
     * All available versions.
     */
    val versions: List<StonecutterProject> get() = tree.versions
    /**
     * The currently active version. Global for all instances of the build file.
     *
     * **May not exist in this branch!**
     */
    val active: StonecutterProject get() = tree.current
    /**
     * Metadata of the currently processed version.
     */
    val current: StonecutterProject by lazy { tree.versions.find { it.project == project.name }
        ?: error("No matching version found for project ${project.name}")
    }

    init {
        // TODO
    }

    override fun swap(identifier: String, replacement: String) {
        data.swaps[validateId(identifier)] = replacement
    }

    override fun const(identifier: String, value: Boolean) {
        data.constants[validateId(identifier)] = value
    }

    override fun dependency(identifier: String, version: String) {
        data.dependencies[validateId(identifier)] = validateSemver(version)
    }

    override fun exclude(path: Path) {
        data.excludedPaths.add(path)
    }

    override fun exclude(path: String) {
        require(path.isNotBlank()) { "Path must not be empty" }
        if (path.startsWith("*.")) data.excludedExtensions.add(path.substring(2))
        else data.excludedPaths.add(project.parent!!.file(path).toPath())
    }

    private fun validateId(id: String) = id.apply {
        require(all(::allowed)) { "Invalid identifier: $this" }
    }

    private fun validateSemver(version: String) = try {
        VersionParser.parse(version)
    } catch (e: VersionParsingException) {
        throw IllegalArgumentException("Invalid semantic version: $version").apply {
            initCause(e)
        }
    }

    private fun configureSources() {
        try {
            val useChiseledSrc = tree.hasChiseled(project.gradle.startParameter.taskNames)
            val formatter: (Path) -> Any = when {
                useChiseledSrc   -> { src -> File(project.buildDirectory, "chiseledSrc/$src") }
                current.isActive -> { src -> "../../src/$src" }
                else             -> return
            }

            val parentDir = project.parent!!.projectDir.resolve("src").toPath()
            val thisDir = project.projectDir.resolve("src").toPath()

            fun applyChiseled(from: SourceDirectorySet, to: SourceDirectorySet = from) {
                from.sourceDirectories.mapNotNull {
                    val relative = thisDir.relativize(it.toPath())
                    if (relative.startsWith(".."))
                        return@mapNotNull if (current.isActive) null
                        else parentDir.relativize(it.toPath())
                    else relative
                }.forEach {
                    to.srcDir(formatter(it))
                }
            }

            for (it in project.property("sourceSets") as SourceSetContainer) {
                applyChiseled(it.allJava, it.java)
                applyChiseled(it.resources)
            }
        } catch (_: MissingPropertyException) {
        }
    }
}