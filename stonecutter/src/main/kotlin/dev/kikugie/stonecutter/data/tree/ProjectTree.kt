@file:Suppress("UNCHECKED_CAST")

package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.locate
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.process.FileProcessor
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.kotlin.dsl.getByType
import java.io.File

private inline fun <K, V, R> Map<K, V>.remap(block: (K, V) -> Pair<K, R>): Set<Map.Entry<K, R>> = buildSet {
    entries.map { (key, value) ->
        block(key, value).let {
            object : Map.Entry<K, R> {
                override val key = it.first
                override val value = it.second
            }
        }
    }
}

internal fun LightTree.withProject(any: Project): ProjectTree = ProjectTree(this, any.locate(hierarchy))

/**
 * Implementation of [NodePrototype] that can access the corresponding Gradle [project].
 * @property light The underlying [LightNode], which can be serialised in tasks with Gradle configuration cache enabled.
 * @property project The corresponding Gradle [Project] instance
 */
public class ProjectNode(public val light: LightNode, public val project: Project) :
    NodePrototype by light {
    /**
     * Stonecutter plugin for this node.
     * @throws UnknownDomainObjectException if the plugin is not applied.
     */
    public val stonecutter: StonecutterBuild get() = project.extensions.getByType<StonecutterBuild>()
    override val branch: ProjectBranch by lazy { light.branch.let { ProjectBranch(it, project.locate(it.hierarchy)) } }

    override fun peer(node: Identifier): ProjectNode? =
        this.branch[node]

    override fun sibling(branch: Identifier): ProjectNode? =
        this.branch.tree[branch][metadata.project]

    override fun find(branch: Identifier, node: Identifier): ProjectNode? =
        this.branch.tree[branch][node]
}

/**
 * Implementation of [BranchPrototype] that can access the corresponding Gradle [project].
 * @property light The underlying [LightBranch], which can be serialised in tasks with Gradle configuration cache enabled.
 * @property project The corresponding Gradle [Project] instance
 */
public class ProjectBranch(public val light: LightBranch, public val project: Project) :
    BranchPrototype<ProjectNode> by light as BranchPrototype<ProjectNode> {
    private val cache: (Identifier) -> ProjectNode? = memoize { id ->
        light[id]?.let { ProjectNode(it, project.locate(it.hierarchy)) }
    }
    override val tree: ProjectTree by lazy { ProjectTree(light.tree, project.locate(hierarchy - id)) }
    override val entries: Set<Map.Entry<Identifier, ProjectNode>> by lazy {
        light.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectNode> by lazy {
        light.values.map { cache(it.metadata.project)!! }
    }

    override val nodes: Collection<ProjectNode> get() = values

    override fun containsValue(value: ProjectNode): Boolean =
        light.containsValue(value.light)

    override fun get(key: Identifier): ProjectNode? = cache(key)
    override fun get(node: ProjectHierarchy): ProjectNode? = light[node]?.let { cache(it.metadata.project) }

    /**Finds the [ProjectNode] corresponding to the given Gradle [project].*/
    public operator fun get(project: Project): ProjectNode? =
        get(project.path.substringAfterLast(':'))
}

/**
 * Implementation of [TreePrototype] that can access the corresponding Gradle [project].
 * @property light The underlying [LightTree], which can be serialised in tasks with Gradle configuration cache enabled.
 * @property project The corresponding Gradle [Project] instance
 */
public class ProjectTree(public val light: LightTree, public val project: Project) :
    TreePrototype<ProjectBranch> by light as TreePrototype<ProjectBranch> {
    internal var configured: Boolean = false
    internal var provider: File? = null
        set(value) {
            if (value == null) return.also { field = null }
            val line = requireNotNull(value.useLines { it.firstOrNull() }) {
                "Provided file ${value.invariantSeparatorsPath} must specify the active version in the first line"
            }
            current = getByName(line.trim())
        }

    private val cache: (Identifier) -> ProjectBranch? = memoize { id ->
        light[id]?.let { ProjectBranch(it, project.locate(it.hierarchy)) }
    }
    override val entries: Set<Map.Entry<Identifier, ProjectBranch>> by lazy {
        light.remap { id, _ -> id to cache(id)!! }
    }
    override val values: Collection<ProjectBranch> by lazy {
        light.values.map { cache(it.id)!! }
    }

    override var current: StonecutterProject
        get() = light.current
        internal set(value) = with(light) {
            configured = true
            current.isActive = false
            value.isActive = true
            light.current = value
        }
    override val branches: Collection<ProjectBranch> get() = values
    override val nodes: Collection<ProjectNode> get() = values.flatMap { it.nodes }

    override fun containsValue(value: ProjectBranch): Boolean =
        light.containsValue(value.light)

    override fun get(key: Identifier): ProjectBranch? = cache(key)
    override fun get(branch: ProjectHierarchy): ProjectBranch? = light[branch]?.let { cache(it.id) }

    /**Finds the [ProjectBranch] corresponding to the given Gradle [project].*/
    public operator fun get(project: Project): ProjectBranch? =
        cache(project.path.removePrefix(this.project.path).removeStarting(':'))

    internal fun getByName(name: Identifier) = requireNotNull(versions.find { it.project == name }) {
        "Node '$name' is not registered in project $hierarchy"
    }
}