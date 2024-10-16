package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.settings.builder.TreeBuilder
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
    private val _branches: Map<Identifier, ProjectBranch>,
) : Map<Identifier, ProjectBranch> by _branches, Project by project {
    /**
     * Location of this tree on the disk.
     */
    val path: Path = projectDir.toPath()

    /**
     * All registered project nodes.
     */
    val nodes: Collection<ProjectNode> = values.flatMap { it.values }

    /**
     * All registered branches
     */
    val branches: Collection<ProjectBranch> = values

    /**
     * All unique versions in this tree.
     */
    val versions: Collection<StonecutterProject> = values.flatMap { it.versions }.toSet()

    /**
     * The active version for this tree.
     */
    var current: StonecutterProject = vcs
        internal set

    init {
        values.forEach { it.tree = this }
    }

    /**
     * Finds a branch for the given project.
     *
     * @param project Project reference
     */
    operator fun get(project: Project) = get(project.path.removePrefix(":"))
}