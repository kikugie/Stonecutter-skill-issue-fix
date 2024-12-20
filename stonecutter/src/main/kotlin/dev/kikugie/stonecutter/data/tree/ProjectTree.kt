@file:Suppress("UNCHECKED_CAST")

package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.locate
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

@StonecutterDelicate internal fun LightNode.withProject(project: Project): ProjectNode = ProjectNode(this, project)
@StonecutterDelicate internal fun LightBranch.withProject(project: Project): ProjectBranch = ProjectBranch(this, project)
@StonecutterDelicate internal fun LightTree.withProject(project: Project): ProjectTree = ProjectTree(this, project)

/**Implementation of [NodePrototype] that can access the corresponding Gradle [project].*/
@StonecutterDelicate
class ProjectNode(val light: LightNode, project: Project) :
    NodePrototype by light,
    Project by project {
    /**
     * Stonecutter plugin for this node.
     * @throws UnknownDomainObjectException if the plugin is not applied.
     */
    val stonecutter: StonecutterBuild get() = extensions.getByType<StonecutterBuild>()
    override val branch: ProjectBranch by lazy { light.branch.let { ProjectBranch(it, locate(it.hierarchy)) } }

    override fun peer(node: Identifier): ProjectNode? =
        this.branch[node]

    override fun sibling(branch: Identifier): ProjectNode? =
        this.branch.tree[branch][metadata.project]

    override fun find(branch: Identifier, node: Identifier): ProjectNode? =
        this.branch.tree[branch][node]
}

/**Implementation of [BranchPrototype] that can access the corresponding Gradle [project].*/
@StonecutterDelicate
class ProjectBranch(val light: LightBranch, project: Project) :
    BranchPrototype<ProjectNode> by light as BranchPrototype<ProjectNode>,
    Project by project {
    private val cache: (Identifier) -> ProjectNode? = memoize { id ->
        light[id]?.let { ProjectNode(it, locate(it.hierarchy)) }
    }
    override val tree: ProjectTree by lazy { ProjectTree(light.tree, locate(hierarchy - id)) }
    override val entries: Set<Map.Entry<Identifier, ProjectNode>> by lazy {
        light.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectNode> by lazy {
        light.values.map { cache(it.metadata.project)!! }
    }

    override val nodes: Collection<ProjectNode>
        get() = light.nodes as Collection<ProjectNode>

    override fun get(key: Identifier): ProjectNode? = cache(key)
    override fun containsValue(value: ProjectNode): Boolean =
        light.containsValue(value.light)

    /**Finds the [ProjectNode] corresponding to the given Gradle [project].*/
    operator fun get(project: Project): ProjectNode? =
        get(project.path.substringAfterLast(':'))
}

/**Implementation of [TreePrototype] that can access the corresponding Gradle [project].*/
@StonecutterDelicate
class ProjectTree(val light: LightTree, project: Project) :
    TreePrototype<ProjectBranch> by light as TreePrototype<ProjectBranch>,
    Project by project {
    private val cache: (Identifier) -> ProjectBranch? = memoize { id ->
        light[id]?.let { ProjectBranch(it, locate(it.hierarchy)) }
    }
    override val entries: Set<Map.Entry<Identifier, ProjectBranch>> by lazy {
        light.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectBranch> by lazy {
        light.values.map { cache(it.id)!! }
    }

    override val branches: Collection<ProjectBranch>
        get() = light.branches as Collection<ProjectBranch>
    override val nodes: Collection<ProjectNode>
        get() = light.nodes as Collection<ProjectNode>

    override fun get(key: Identifier): ProjectBranch? = cache(key)
    override fun containsValue(value: ProjectBranch): Boolean =
        light.containsValue(value.light)

    /**Finds the [ProjectBranch] corresponding to the given Gradle [project].*/
    operator fun get(project: Project): ProjectBranch? =
        get(project.path.removePrefix(path).removeStarting(':'))
}