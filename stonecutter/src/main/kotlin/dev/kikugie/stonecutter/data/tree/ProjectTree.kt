@file:Suppress("UNCHECKED_CAST")

package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.memoize
import dev.kikugie.stonecutter.get
import dev.kikugie.stonecutter.removeStarting
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.kotlin.dsl.getByType

private inline fun <K, V, R> Map<K, V>.remap(block: (K, V) -> Pair<K, R>): Set<Map.Entry<K, R>> = buildSet {
    entries.map { (key, value) ->
        block(key, value).let {
            object : Map.Entry<K, R> {
                override val key = it.first;
                override val value = it.second
            }
        }
    }
}

internal fun LightNode.withProject(project: Project): ProjectNode = ProjectNode(this, project)
internal fun LightBranch.withProject(project: Project): ProjectBranch = ProjectBranch(this, project)
internal fun LightTree.withProject(project: Project): ProjectTree = ProjectTree(this, project)

/**Implementation of [NodePrototype] that can access the corresponding Gradle [project].*/
class ProjectNode(internal val delegate: NodePrototype, project: Project) :
    NodePrototype by delegate,
    Project by project {
    /**
     * Stonecutter plugin for this node.
     * @throws UnknownDomainObjectException if the plugin is not applied.
     */
    val stonecutter: StonecutterBuild get() = extensions.getByType<StonecutterBuild>()
    override val branch: ProjectBranch by lazy {
        ProjectBranch(delegate.branch, rootProject.project(delegate.branch.hierarchy))
    }

    override fun peer(node: Identifier): ProjectNode? =
        this.branch[node]

    override fun sibling(branch: Identifier): ProjectNode? =
        this.branch.tree[branch][metadata.project]

    override fun find(branch: Identifier, node: Identifier): ProjectNode? =
        this.branch.tree[branch][node]
}

/**Implementation of [BranchPrototype] that can access the corresponding Gradle [project].*/
class ProjectBranch(internal val delegate: BranchPrototype<out NodePrototype>, project: Project) :
    BranchPrototype<ProjectNode> by delegate as BranchPrototype<ProjectNode>,
    Project by project {
    private val cache: (Identifier) -> ProjectNode? = memoize { id ->
        delegate[id]?.let { ProjectNode(it, rootProject.project(it.hierarchy)) }
    }
    override val tree: ProjectTree by lazy {
        val parent = if (hierarchy.isBlank() || hierarchy == ":") rootProject else rootProject.project(hierarchy)
        ProjectTree(delegate.tree, parent)
    }
    override val entries: Set<Map.Entry<Identifier, ProjectNode>> by lazy {
        delegate.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectNode> by lazy {
        delegate.values.map { cache(it.metadata.project)!! }
    }

    override val nodes: Collection<ProjectNode>
        get() = delegate.nodes as Collection<ProjectNode>
    override fun get(key: Identifier): ProjectNode? = cache(key)
    override fun containsValue(value: ProjectNode): Boolean =
        delegate.containsValue(value.delegate)

    /**Finds the [ProjectNode] corresponding to the given Gradle [project].*/
    operator fun get(project: Project): ProjectNode? =
        get(project.path.substringAfterLast(':'))
}

/**Implementation of [TreePrototype] that can access the corresponding Gradle [project].*/
class ProjectTree(internal val delegate: TreePrototype<out BranchPrototype<out NodePrototype>>, project: Project) :
    TreePrototype<ProjectBranch> by delegate as TreePrototype<ProjectBranch>,
    Project by project {
    private val cache: (Identifier) -> ProjectBranch? = memoize { id ->
        delegate[id]?.let { ProjectBranch(it, rootProject.project(it.hierarchy)) }
    }
    override val entries: Set<Map.Entry<Identifier, ProjectBranch>> by lazy {
        delegate.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectBranch> by lazy {
        delegate.values.map { cache(it.id)!! }
    }

    override val branches: Collection<ProjectBranch>
        get() = delegate.branches as Collection<ProjectBranch>
    override val nodes: Collection<ProjectNode>
        get() = delegate.nodes as Collection<ProjectNode>
    override fun get(key: Identifier): ProjectBranch? = cache(key)
    override fun containsValue(value: ProjectBranch): Boolean =
        delegate.containsValue(value.delegate)

    /**Finds the [ProjectBranch] corresponding to the given Gradle [project].*/
    operator fun get(project: Project): ProjectBranch? =
        get(project.path.removePrefix(path).removeStarting(':'))
}