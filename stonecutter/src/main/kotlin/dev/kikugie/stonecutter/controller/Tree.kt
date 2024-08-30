package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TaskName
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.sanitize
import dev.kikugie.stonecutter.settings.TreeBuilder
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path

internal operator fun ProjectBranch?.get(project: ProjectName) = this?.nodes?.get(project)

/**
 * Finalized project tree created by [StonecutterController].
 * Unlike the [TreeBuilder], this one is immutable and contains addition information for the plugin.
 *
 * @property project Gradle project of this tree, where `stonecutter.gradle[.kts]` is located
 * @property vcs Version control project for this tree
 * @property branches All registered branches.
 * Each branch may have the same [versions] or a subset of it. The default branch is `""`
 */
data class ProjectTree(
    private val project: Project,
    val vcs: StonecutterProject,
    val branches: Map<ProjectName, ProjectBranch>,
) : Iterable<ProjectBranch>, Project by project {
    /**
     * Location of this tree on the disk.
     */
    val path: Path = projectDir.toPath()

    /**
     * All registered project nodes.
     */
    val nodes: List<ProjectNode> = flatMap { it.nodes.values }

    /**
     * All unique versions in this tree.
     */
    val versions: Collection<StonecutterProject> = flatMap { it.versions }.toSet()

    /**
     * The active version for this tree.
     */
    var current: StonecutterProject = vcs
        internal set

    private val tasks: MutableSet<TaskName> = mutableSetOf()

    init {
        branches.values.forEach { it.tree = this }
    }

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

    /**
     * Iterator for the [branches] values.
     */
    override fun iterator(): Iterator<ProjectBranch> = branches.values.iterator()
}

/**
 * Branch in a [ProjectTree], containing registered nodes.
 *
 * @property project Gradle project of this branch.
 * **May be the same as [ProjectTree.project] if this is the root branch**
 * @property id Name of this branch. Same as the key in [ProjectTree.branches].
 * **Empty string for the root branch**
 * @property nodes Nodes accessible by their project names
 */
data class ProjectBranch(
    private val project: Project,
    val id: ProjectName,
    val nodes: Map<ProjectName, ProjectNode>,
): Iterable<ProjectNode>, Project by project {
    /**
     * Location of this branch on the disk.
     */
    val path: Path = projectDir.toPath()

    /**
     * [StonecutterProject] instances of all [nodes].
     */
    val versions: Collection<StonecutterProject> = map { it.metadata }

    /**
     * Reference to the tree containing this branch.
     */
    lateinit var tree: ProjectTree
        internal set

    init {
        nodes.values.forEach { it.branch = this }
    }

    /**
     * Finds an entry for the given name.
     *
     * @param project Name of the project
     */
    operator fun get(project: ProjectName) = nodes[project]

    /**
     * Finds an entry for the given project.
     *
     * @param project Project reference
     */
    operator fun get(project: Project) = nodes[project.path.substringAfterLast(':')]

    /**
     * Iterator for the [nodes] values.
     */
    override fun iterator(): Iterator<ProjectNode> = nodes.values.iterator()
}

/**
 * Endpoint of the tree that represents a single version.
 *
 * @property project Project of this node, located in `version/$it`
 * @property metadata Project metadata used for configuring tasks
 */
data class ProjectNode(
    private val project: Project,
    val metadata: StonecutterProject
) : Project by project {
    /**
     * Location of this node on the disk.
     */
    val path: Path = projectDir.toPath()

    /**
     * Stonecutter plugin for this node.
     * @throws [UnknownDomainObjectException] if the plugin is not applied
     */
    val stonecutter: StonecutterBuild get() = extensions.getByType<StonecutterBuild>()

    /**
     * Reference to the branch containing this node.
     */
    lateinit var branch: ProjectBranch
        internal set

    /**
     * Finds the node with the given name in the same branch.
     *
     * @param node Name of the node. Should be an existing [StonecutterProject.project]
     * @return Found [ProjectNode] or null if it doesn't exist
     */
    fun peer(node: ProjectName): ProjectNode? = branch.nodes[node]

    /**
     * Finds the same node in the given branch.
     *
     * @param branch Branch name. `""` targets the root branch
     * @return Found [ProjectNode] or null if it doesn't exist
     */
    fun sibling(branch: ProjectName): ProjectNode? = this.branch.tree[branch][metadata.project]

    /**
     * Finds the given node in another branch.
     *
     * @param branch Branch name. `""` targets the root branch
     * @param node Name of the node. Should be an existing [StonecutterProject.project]
     * @return Found [ProjectNode] or null if it doesn't exist
     */
    fun find(branch: ProjectName, node: ProjectName): ProjectNode? = this.branch.tree[branch][node]
}
