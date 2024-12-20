package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import java.nio.file.Path

/**Default implementation of the [NodePrototype]. Other variants like [ProjectNode] can access it via composition or delegation.*/
data class LightNode(
    override val location: Path,
    override val metadata: StonecutterProject
) : NodePrototype {
    override lateinit var branch: LightBranch; internal set
    override val hierarchy: ProjectHierarchy get() = branch.hierarchy + metadata.project

    override fun peer(node: Identifier): LightNode? =
        this.branch[node]

    override fun sibling(branch: Identifier): LightNode? =
        this.branch.tree[branch][metadata.project]

    override fun find(branch: Identifier, node: Identifier): LightNode? =
        this.branch.tree[branch][node]
}

/**Default implementation of the [BranchPrototype]. Other variants like [ProjectBranch] can access it via composition or delegation.*/
data class LightBranch(
    override val location: Path,
    override val id: Identifier,
    private val delegate: Map<Identifier, LightNode>
) : BranchPrototype<LightNode>, Map<Identifier, LightNode> by delegate {
    override lateinit var tree: LightTree; internal set
    override val hierarchy: ProjectHierarchy get() = tree.hierarchy + id
    override val nodes: Collection<LightNode> get() = values
    override val versions: Collection<StonecutterProject> get() = values.map { it.metadata }
}

/**Default implementation of the [TreePrototype]. Other variants like [ProjectTree] can access it via composition or delegation.*/
data class LightTree(
    override val location: Path,
    override val hierarchy: ProjectHierarchy,
    override val vcs: StonecutterProject,
    private val delegate: Map<Identifier, LightBranch>
) : TreePrototype<LightBranch>, Map<Identifier, LightBranch> by delegate {
    override var current: StonecutterProject = vcs; internal set
    override val branches: Collection<LightBranch> get() = values
    override val nodes: Collection<LightNode> get() = values.flatMap { it.values }
    override val versions: Collection<StonecutterProject> get() = values.flatMap { it.versions }.toSet()
}