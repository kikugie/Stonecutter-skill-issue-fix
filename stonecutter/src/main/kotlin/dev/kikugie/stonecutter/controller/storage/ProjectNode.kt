package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.StonecutterBuild
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path

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