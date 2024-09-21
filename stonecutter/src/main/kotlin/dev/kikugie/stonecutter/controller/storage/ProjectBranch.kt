package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import org.gradle.api.Project
import java.nio.file.Path

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