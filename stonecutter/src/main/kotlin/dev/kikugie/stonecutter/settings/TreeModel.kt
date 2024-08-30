package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TargetVersion
import dev.kikugie.stonecutter.data.NodeMap
import dev.kikugie.stonecutter.sanitize
import org.gradle.api.Action

/**
 * Represents a project tree structure in the `settings.gradle[.kts]` file.
 * This tree only supports 3 layers of depth: `root -> branch -> node`.
 */
class TreeBuilder : ProjectProvider {
    private val versions: MutableMap<StonecutterProject, StonecutterProject> = mutableMapOf()
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
    private fun find(project: StonecutterProject) = versions.getOrDefault(project, project)

    private fun add(name: ProjectName, project: StonecutterProject) =
        nodes.getOrPut(name, ::mutableSetOf).let { versions[project] = project; it += project }

    override fun vers(name: ProjectName, version: TargetVersion) =
        add("", find(StonecutterProject(name, version)))

    /**
     * Creates an inherited branch, which copies all the versions specified in this block.
     *
     * @param name Subproject's name for this branch
     */
    fun branch(name: ProjectName) = branch(name.sanitize()) { inherit() }

    /**
     * Creates a new branch in this tree with provided configuration.
     *
     * @param name Subproject's name for this branch
     * @param action Branch configuration
     */
    fun branch(name: ProjectName, action: Action<BranchBuilder>) =
        BranchBuilder(name.sanitize()).also { branches[name.sanitize()] = it }.let(action::execute)

    override fun toString() = buildString {
        appendLine("|- vcs: $vcsVersion")
        appendLine("|- versions:")
        appendLine(versions.values.treeView().prepend("| "))
        appendLine("\\- branches:")
        append(nodes.treeView().prepend("  "))
    }

    /**
     * Proxy class for adding nodes to the given branch in the tree.
     *
     * @param id Subproject's name for this branch
     */
    inner class BranchBuilder(private val id: ProjectName) :
        ProjectProvider {
        /**
         * Buildscript filename override for this branch.
         * Defaults to the one set in the [StonecutterSettings] configuration scope.
         */
        lateinit var buildscript: String

        override fun vers(
            name: ProjectName,
            version: TargetVersion
        ) =
            this@TreeBuilder.add(id, find(StonecutterProject(name, version)))

        /**
         * Copies nodes registered in [TreeBuilder] to this branch
         */
        fun inherit() = checkNotNull(this@TreeBuilder.nodes[""]) {
            "No root node to inherit from"
        }.forEach { this@TreeBuilder.add(id, it) }
    }
}

private fun String.prepend(str: String) = lines().joinToString("\n") { str + it }

private fun StonecutterProject.treeView() = "- $project: v$version"

private fun Collection<StonecutterProject>.treeView() =
    map { it.treeView() }.mapIndexed { j, line ->
        val lastLine = j == size - 1
        if (!lastLine) line.prepend("|")
        else line.prepend(" ").replaceFirst(' ', '\\')
    }.joinToString("\n")

private fun NodeMap.treeView() = entries.mapIndexed { i, entry ->
    val (name, nodes) = entry
    val joined = nodes.treeView()
    val lastElem = i == this@treeView.size - 1
    val elem = if (lastElem) joined.prepend("  ")
    else joined.prepend("| ")
    val header = if (lastElem) "\\- $name:\n"
    else "|- $name:\n"
    header + elem
}.joinToString("\n")