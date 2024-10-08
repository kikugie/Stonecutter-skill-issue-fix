package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.storage.*
import dev.kikugie.stonecutter.data.ProjectParameterContainer
import dev.kikugie.stonecutter.data.ProjectTreeContainer
import dev.kikugie.stonecutter.process.StonecutterTask
import dev.kikugie.stonecutter.stonecutterCachePath
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Stonecutter plugin applied to the versioned buildscript.
 *
 * @property project This plugin's project
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration">Wiki</a>
 */
@OptIn(ExperimentalPathApi::class)
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterBuild(val project: Project) : BuildConfiguration(project.parent!!), StonecutterUtility {
    private val parent: Project = requireNotNull(project.parent) {
        "StonecutterBuild applied to a non-versioned buildscript"
    }

    private val params: GlobalParameters = requireNotNull(project.gradle.extensions.getByType<ProjectParameterContainer>()[project]) {
        "Global parameters not found for project '$project'"
    }

    /**
     * The full tree this project belongs to. Without subprojects, it will only have the root branch.
     * Allows traversing all branches if needed. For project access use [node] methods.
     */
    val tree: ProjectTree = requireNotNull(project.gradle.extensions.getByType<ProjectTreeContainer>()[project]) {
        "Project '$project' is not versioned"
    }
    /**
     * The branch this project belongs to. Allows accessing other versions.
     * For most cases use [node] methods.
     */
    val branch: ProjectBranch = requireNotNull(tree[parent]) {
        "Branch '$parent' not found in [${tree.branches.keys.joinToString { "'$it'" }}]"
    }

    /**
     * This project's node. Contains this project's metadata and provides API for traversing the tree.
     */
    val node: ProjectNode = requireNotNull(branch[project]) {
        "Project '$project' is not found in the branch {${branch.nodes.keys.joinToString { "'$it'" }}]"
    }

    /**
     * All version in this project's branch.
     */
    val versions: Collection<StonecutterProject> get() = branch.versions

    /**
     * The currently active version. Global for all instances of the build file.
     *
     * **May not exist in this branch!**
     */
    val active: StonecutterProject get() = tree.current

    /**
     * Metadata of the currently processed version.
     */
    val current: StonecutterProject = node.metadata

    init {
        project.configure()
    }

    private fun Project.configure() {
        tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            debug.set(params.debug)
            toVersion.set(current)
            fromVersion.set(active)

            input.set("src")
            project.buildDirectoryPath.resolve("chiseledSrc").let {
                if (it.exists()) it.deleteRecursively()
                output.set(branch.path.relativize(it).invariantSeparatorsPathString)
            }

            data.set(mapOf(branch to this@StonecutterBuild.data))
            sources.set(mapOf(branch to branch.path))
            cacheDir.set { _, version -> branch[version.project]?.project?.stonecutterCachePath
                ?: branch.project.stonecutterCachePath.resolve("out-of-bounds/$version")
            }
        }

        afterEvaluate { configureSources() }
    }

    private fun Project.configureSources() {
        try {
            val useChiseledSrc = params.hasChiseled(gradle.startParameter.taskNames)
            val formatter: (Path) -> Any = when {
                useChiseledSrc -> { src -> File(buildDirectoryFile, "chiseledSrc/$src") }
                current.isActive -> { src -> "../../src/$src" }
                else -> return
            }

            val parentDir = parent!!.projectDir.resolve("src").toPath()
            val thisDir = projectDir.resolve("src").toPath()

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

            for (it in property("sourceSets") as SourceSetContainer) {
                applyChiseled(it.allJava, it.java)
                applyChiseled(it.resources)
            }
        } catch (_: MissingPropertyException) {
        }
    }
}