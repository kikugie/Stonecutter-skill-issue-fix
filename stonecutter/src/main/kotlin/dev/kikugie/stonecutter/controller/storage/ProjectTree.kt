package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TaskName
import dev.kikugie.stonecutter.sanitize
import org.gradle.api.Project
import java.nio.file.Path

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
) : Collection<ProjectBranch> by branches.values, Project by project {
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
}