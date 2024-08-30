package dev.kikugie.stonecutter.build

import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.lexer.IdentifierRecognizer.Companion.allowed
import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.ProjectBranch
import dev.kikugie.stonecutter.controller.ProjectNode
import dev.kikugie.stonecutter.controller.ProjectTree
import dev.kikugie.stonecutter.data.StitcherParameters
import dev.kikugie.stonecutter.data.TreeContainer
import dev.kikugie.stonecutter.data.buildDirectoryPath
import dev.kikugie.stonecutter.process.StonecutterTask
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
open class StonecutterBuild(val project: Project) : BuildConfiguration, StonecutterUtility {
    private val parent = checkNotNull(project.parent) {
        "StonecutterBuild applied to a non-versioned buildscript"
    }
    internal lateinit var data: StitcherParameters

    /**
     * The full tree this project belongs to. Without subprojects it will only have the root branch.
     * Allows traversing all branches if needed. For project access use [node] methods.
     */
    val tree: ProjectTree = requireNotNull(project.rootProject.run { gradle.extensions.getByType<TreeContainer>()[this] }) {
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

    private fun Project.configure() {
        tasks.register("setupChiseledBuild", StonecutterTask::class.java) {
            toVersion.set(current)
            fromVersion.set(active)

            input.set("src")
            project.buildDirectoryPath.resolve("chiseledSrc").let {
                if (it.exists()) it.deleteRecursively()
                output.set(branch.path.relativize(it).invariantSeparatorsPathString)
            }

            data.set(mapOf(branch to this@StonecutterBuild.data))
            sources.set(mapOf(branch to node.path))
            cacheDir.set { _, version ->
                branch[version.project]?.project?.stonecutterCachePath
                    ?: branch.project.stonecutterCachePath.resolve("out-of-bounds/$version")
            }
        }

        afterEvaluate { configureSources() }
    }

    private fun Project.configureSources() {
        try {
            val useChiseledSrc = tree.hasChiseled(gradle.startParameter.taskNames)
            val formatter: (Path) -> Any = when {
                useChiseledSrc -> { src -> File(buildDirectory, "chiseledSrc/$src") }
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