package dev.kikugie.stonecutter.settings.builder

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.settings.ProjectProvider
import org.gradle.api.Action

internal typealias Nodes = MutableSet<StonecutterProject>
internal typealias NodeMap = MutableMap<Identifier, Nodes>

/**
 * Represents a project tree structure in the `settings.gradle[.kts]` file.
 * This tree only supports three layers of depth: `root -> branch -> node`.
 */
class TreeBuilder internal constructor() : ProjectProvider {
    internal val versions: MutableMap<StonecutterProject, StonecutterProject> = mutableMapOf()
    internal val nodes: NodeMap = mutableMapOf()
    internal val branches: MutableMap<Identifier, BranchBuilder> = mutableMapOf()

    /**Version used by the `Reset active project` task. Defaults to the first registered version.*/
    var vcsVersion: AnyVersion? = null
        get() = field ?: versions.values.firstOrNull()?.project
        set(value) {
            requireNotNull(value) { "`vcsVersion` must be set to a non-null value." }
            require(value in versions.values.map { it.project }) { "Version $value is not registered." }
            field = value
        }
    internal val vcsProject
        get() = versions.values.find { it.project == vcsVersion }
            ?: error("No versions registered")


    internal fun add(branch: Identifier, project: StonecutterProject) =
        nodes.getOrPut(branch, ::mutableSetOf).let { node ->
            require(node.none { it.project == project.project }) { "Duplicate project identifier '${project.project}' in branch '$branch'" }
            node += versions.getOrPut(project) { project }
        }

    override fun vers(name: Identifier, version: AnyVersion) =
        add("", StonecutterProject(name, version))

    /**Creates an inherited branch, which copies all the versions specified in this block.*/
    fun branch(name: Identifier) = branch(name) { inherit() }

    /**Creates a new branch in this tree with the provided configuration.*/
    fun branch(name: Identifier, action: Action<BranchBuilder>) {
        require(name.isNotBlank()) { "Branch name cannot be blank" }
        require(name.isValid()) { "Invalid branch name: '$name'" }
        branches.getOrPut(name) { BranchBuilder(this, name) }.let(action::execute)
    }
}