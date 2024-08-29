package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TaskName
import dev.kikugie.stonecutter.sanitize
import dev.kikugie.stonecutter.settings.TreeBuilder
import org.gradle.api.Project
import java.nio.file.Path

internal operator fun ProjectBranch?.get(project: ProjectName) = this?.entries?.get(project)

/**
 * Finalized project tree created by [StonecutterController].
 * Unlike the [TreeBuilder], this one is immutable and contains addition information for the plugin.
 *
 * @property project Gradle project of this tree, where `stonecutter.gradle[.kts]` is located
 * @property vcs Version control project for this tree
 * @property branches Registered branches
 */
data class ProjectTree(
    val project: Project,
    val vcs: StonecutterProject,
    val branches: Map<ProjectName, ProjectBranch>,
) {
    /**
     * Location of this tree on the disk.
     */
    val path: Path by lazy { project.projectDir.toPath() }

    /**
     * All registered nodes.
     */
    val nodes: Sequence<ProjectNode> get() = branches.values.asSequence().flatMap { it.entries.values }

    /**
     * The active version for this tree.
     */
    var current: StonecutterProject = vcs
        internal set

    /**
     * All registered versions.
     */
    var versions: List<StonecutterProject> = emptyList()
        internal set

    private val tasks: MutableSet<TaskName> = mutableSetOf()

    /**
     * Finds a branch for the given name.
     *
     * @param project Name of the project
     */
    operator fun get(project: ProjectName) = branches[project]

    /**
     * Finds a branch for the given project.
     *
     * @param project Project reference
     */
    operator fun get(project: Project) = branches[project.path.sanitize()]

    internal fun addTask(task: TaskName) = tasks.add(task)
    internal fun hasChiseled(stack: Iterable<TaskName>) = stack.any { it in tasks }
}

/**
 * Branch in a [ProjectTree], containing registered nodes.
 *
 * @property project Gradle project of this branch.
 * **May be the same as [ProjectTree.project] if this is the root branch**
 * @property name Name of this branch. Same as the key in [ProjectTree.branches].
 * **Empty string for the root branch**
 * @property entries Nodes accessible by their project names
 */
data class ProjectBranch(
    val project: Project,
    val name: ProjectName,
    val entries: Map<ProjectName, ProjectNode>,
) {
    /**
     * Location of this branch on the disk.
     */
    val path by lazy { project.projectDir.toPath() }

    /**
     * Finds an entry for the given name.
     *
     * @param project Name of the project
     */
    operator fun get(project: ProjectName) = entries[project]

    /**
     * Finds an entry for the given project.
     *
     * @param project Project reference
     */
    operator fun get(project: Project) = entries[project.path.sanitize()]
}

/**
 * Endpoint of the tree that represent a single version.
 *
 * @property project Project of this node, located in `version/$it`
 * @property parent Name of the parent project. Can be used to find this node in the [ProjectBranch]
 * @property metadata Project metadata used for configuring tasks
 */
data class ProjectNode(
    val project: Project,
    val parent: ProjectName,
    val metadata: StonecutterProject
) {
    /**
     * Location of this node on the disk.
     */
    val path by lazy { project.projectDir.toPath() }
}


