package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.storage.*
import dev.kikugie.stonecutter.data.model.BranchInfo.Companion.toBranchInfo
import dev.kikugie.stonecutter.data.model.NodeModel
import dev.kikugie.stonecutter.data.container.ProjectParameterContainer
import dev.kikugie.stonecutter.data.container.ProjectTreeContainer
import dev.kikugie.stonecutter.process.StonecutterTask
import dev.kikugie.stonecutter.stonecutterCachePath
import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString

// link: wiki-build
/**
 * Stonecutter plugin applied to the versioned buildscript.
 *
 * @property project This plugin's project
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#versioning-build-gradle-kts">Wiki page</a>
 */
@OptIn(ExperimentalPathApi::class)
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterBuild(val project: Project) : BuildConfiguration(project.parent!!), StonecutterUtility {
    private val parent: Project = requireNotNull(project.parent) {
        "StonecutterBuild applied to a non-versioned buildscript"
    }

    private val params: GlobalParameters =
        requireNotNull(project.gradle.extensions.getByType<ProjectParameterContainer>()[project]) {
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
        "Branch '$parent' not found in [${tree.keys.joinToString { "'$it'" }}]"
    }

    /**This project's node. Contains this project's metadata and provides an interface for traversing the tree.*/
    val node: ProjectNode = requireNotNull(branch[project]) {
        "Project '$project' is not found in the branch {${branch.keys.joinToString { "'$it'" }}]"
    }

    /**All versions in this project's branch.*/
    val versions: Collection<StonecutterProject> get() = branch.versions

    /**The currently active version. Global for all instances of the build file.*/
    val active: StonecutterProject get() = tree.current

    /**Metadata of the currently processed version.*/
    val current: StonecutterProject = node.metadata

    init {
        project.configure()
    }

    private fun Project.configure() {
        tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            params.set(this@StonecutterBuild.params)
            toVersion.set(current)
            fromVersion.set(active)

            input.set("src")
            project.buildDirectoryPath.resolve("chiseledSrc").let {
                if (it.exists()) it.deleteRecursively()
                output.set(branch.path.relativize(it).invariantSeparatorsPathString)
            }

            data.set(mapOf(branch to this@StonecutterBuild.data))
            sources.set(mapOf(branch to branch.path))
            cacheDir.set { branch, version -> branch.cachePath(version) }

            doFirst {
                buildDirectoryPath.resolve("chiseledSrc").runCatching {
                    if (exists()) deleteRecursively()
                }.onFailure {
                    logger.warn("Failed to clean chiseledSrc", it)
                }
            }
        }

        afterEvaluate {
            configureSources()
            serializeNode()
        }
    }

    private fun Project.configureSources() {
        try {
            val useChiseledSrc = params.process && params.hasChiseled(gradle.startParameter.taskNames)
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

            for (src in property("sourceSets") as SourceSetContainer) {
                applyChiseled(src.allJava, src.java)
                applyChiseled(src.resources)
                src.extensions.extensionsSchema
                    .filter { it.publicType.concreteClass.interfaces.contains(SourceDirectorySet::class.java) }
                    .forEach { applyChiseled(src.extensions[it.name] as SourceDirectorySet) }
            }
        } catch (_: MissingPropertyException) {
        }
    }

    private fun serializeNode() {
        NodeModel(
            current,
            node.location.relativize(tree.path),
            branch.toBranchInfo(node.location.relativize(branch.path)),
            current.isActive,
            data,
        ).save(node.stonecutterCachePath).onFailure {
            node.logger.warn("Failed to save node model for '${branch.name}:${current.project}'", it)
        }
    }
}