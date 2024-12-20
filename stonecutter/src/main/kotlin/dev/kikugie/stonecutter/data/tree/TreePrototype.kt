package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import org.gradle.api.Project
import java.nio.file.Path

/**Represents properties of a Gradle project.*/
interface GradleMember {
    /**Project location on the disk relative to the root.*/
    val location: Path

    /**Project path in Gradle notation. I.e. `:subproject1:subproject2`*/
    val hierarchy: ProjectHierarchy
}

/**
 * Represents the node data and functionality available at the configuration stage.
 *
 * It's available as [ProjectNode], which serves as an access point for the
 * corresponding Gradle [Project], and as [LightNode], which only stores the data.
 */
interface NodePrototype : GradleMember {
    /**Subproject name and assigned version for this node.*/
    val metadata: StonecutterProject

    /**Reference to the container branch.*/
    val branch: BranchPrototype<out NodePrototype>

    /**Finds the given [node] in the current branch.*/
    fun peer(node: Identifier): NodePrototype?

    /**Finds the node with the same metadata in the given [branch].*/
    fun sibling(branch: Identifier): NodePrototype?

    /**Finds the given [node] in the [branch].*/
    fun find(branch: Identifier, node: Identifier): NodePrototype?
}

/**
 * Represents the branch data and functionality available at the configuration stage.
 *
 * It's available as [ProjectBranch], which serves as an access point for the
 * corresponding Gradle [Project], and as [LightBranch], which only stores the data.
 * @param T [NodePrototype] kind required for the correct [Map] implementation
 */
interface BranchPrototype<T> : GradleMember,
    Map<Identifier, T> where T : NodePrototype {
    /**Unique identifier for this branch, which serves as a subproject name as well. For the root branch it's `""`.*/
    val id: Identifier

    /**Reference to the container tree.*/
    val tree: TreePrototype<out BranchPrototype<out NodePrototype>>

    /**All nodes in the branch.*/
    val nodes: Collection<NodePrototype>

    /**All versions registered in the branch.*/
    val versions: Collection<StonecutterProject>
}

/**
 * Represents the tree data and functionality available at the configuration stage.
 *
 * It's available as [ProjectTree], which serves as an access point for the
 * corresponding Gradle [Project], and as [LightTree], which only stores the data.
 * @param T [BranchPrototype] kind required for the correct [Map] implementation
 */
interface TreePrototype<T> : GradleMember,
    Map<Identifier, T> where T : BranchPrototype<out NodePrototype> {
    /**Version control version. It's used by the `Reset active version` task.*/
    val vcs: StonecutterProject

    /**Currently active version set in `stonecutter.gradle[.kts]`.*/
    val current: StonecutterProject

    /**All branches in the tree.*/
    val branches: Collection<T>

    /**All nodes across all branches in the tree.*/
    val nodes: Collection<NodePrototype>

    /**All unique versions in the tree.*/
    val versions: Collection<StonecutterProject>
}
