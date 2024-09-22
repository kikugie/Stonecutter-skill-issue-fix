package dev.kikugie.stonecutter.settings.builder

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TargetVersion
import dev.kikugie.stonecutter.sanitize
import dev.kikugie.stonecutter.settings.ProjectProvider
import org.gradle.api.Action

internal typealias Nodes = MutableSet<StonecutterProject>
internal typealias NodeMap = MutableMap<ProjectName, Nodes>

/**
 * Represents a project tree structure in the `settings.gradle[.kts]` file.
 * This tree only supports 3 layers of depth: `root -> branch -> node`.
 */
class TreeBuilder : ProjectProvider {
    internal val versions: MutableMap<StonecutterProject, StonecutterProject> = mutableMapOf()
    internal val nodes: NodeMap = mutableMapOf()
    internal val branches: MutableMap<ProjectName, BranchBuilder> = mutableMapOf()

    /**
     * Version used by the `Reset active project` task. Defaults to the first registered version.
     */
    var vcsVersion: ProjectName? = null
        get() = if (field == null) versions.values.firstOrNull()?.project else field
        set(value) {
            requireNotNull(value) { "`vcsVersion` must be set to a non-null value." }
            require(value in versions.values.map { it.project }) { "Version $value is not present in the tree." }
            field = value
        }
    internal val vcsProject
        get() = versions.values.find { it.project == vcsVersion }
            ?: error("No versions registered")

    // Required to have object identity
    internal fun find(project: StonecutterProject) = versions.getOrDefault(project, project)

    internal fun add(name: ProjectName, project: StonecutterProject) =
        nodes.getOrPut(name, ::mutableSetOf).let { versions[project] = project; it += project }

    override fun vers(name: ProjectName, version: TargetVersion) =
        add("", find(StonecutterProject(name, version)))

    /**
     * Creates an inherited branch, which copies all the versions specified in this block.
     *
     * @param name Subproject's name for this branch
     */
    fun branch(name: ProjectName) = branch(name) { inherit() }

    /**
     * Creates a new branch in this tree with the provided configuration.
     *
     * @param name Subproject's name for this branch
     * @param action Branch configuration
     */
    fun branch(name: ProjectName, action: Action<BranchBuilder>) =
        BranchBuilder(this, name.sanitize()).also { branches[name.sanitize()] = it }.let(action::execute)
}