package dev.kikugie.experimentalstonecutter.controller

import dev.kikugie.experimentalstonecutter.ProjectName
import dev.kikugie.experimentalstonecutter.StonecutterProject
import dev.kikugie.experimentalstonecutter.settings.TreeBuilder
import dev.kikugie.experimentalstonecutter.TaskName
import dev.kikugie.experimentalstonecutter.sanitize
import org.gradle.api.Project

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
    val path by lazy { project.projectDir.toPath() }
    val nodes get() = branches.values.asSequence().flatMap { it.entries.values }
    /**
     * The active version for this tree.
     */
    var current: StonecutterProject = vcs
        internal set

    var versions: List<StonecutterProject> = emptyList()
        internal set

    private val tasks: MutableSet<TaskName> = mutableSetOf()

    operator fun get(project: ProjectName) = branches[project]
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
    val path by lazy { project.projectDir.toPath() }
    operator fun get(project: ProjectName) = entries[project]
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
    val path by lazy { project.projectDir.toPath() }
}


