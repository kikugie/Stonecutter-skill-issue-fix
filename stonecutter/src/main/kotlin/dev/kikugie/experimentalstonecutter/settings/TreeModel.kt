package dev.kikugie.experimentalstonecutter.settings

import dev.kikugie.experimentalstonecutter.ProjectName
import dev.kikugie.experimentalstonecutter.StonecutterProject
import dev.kikugie.experimentalstonecutter.TargetVersion
import org.gradle.api.Action

typealias Nodes = MutableSet<StonecutterProject>
typealias NodeMap = MutableMap<ProjectName, Nodes>

/**
 * Represents a project tree structure in the `settings.gradle[.kts]` file.
 * This tree only supports 3 layers of depth: `root -> branch -> node`.
 */
class TreeBuilder : ProjectProvider {
    internal val nodes: NodeMap = mutableMapOf()
    internal val branches: MutableMap<ProjectName, BranchBuilder> = mutableMapOf()
    internal val versions: MutableSet<StonecutterProject> = mutableSetOf()

    /**
     * Version used by the `Reset active project` task. Defaults to the first registered version.
     */
    var vcsVersion: ProjectName? = null
        get() = if (field == null) versions.firstOrNull()?.project else field
        set(value) {
            requireNotNull(value) { "`vcsVersion` must be set to a non-null value." }
            require(value in versions.map { it.project }) { "Version $value is not present in the tree." }
            field = value
        }
    internal val vcsProject
        get() = versions.find { it.project == vcsVersion }
            ?: error("No versions registered")

    private fun add(name: ProjectName, project: StonecutterProject) =
        nodes.getOrPut(name, ::mutableSetOf).let { versions += project; it += project }

    override fun vers(name: ProjectName, version: TargetVersion) =
        add("", StonecutterProject(name, version))

    /**
     * Creates an inherited branch, which copies all the versions specified in this block.
     *
     * @param name Subproject's name for this branch
     */
    fun branch(name: ProjectName) = branch(name) { inherit() }

    /**
     * Creates a new branch in this tree with provided configuration.
     *
     * @param name Subproject's name for this branch
     * @param action Branch configuration
     */
    fun branch(name: ProjectName, action: Action<BranchBuilder>) =
        BranchBuilder(name).also { branches[name] = it }.let(action::execute)

    override fun toString() = buildString {
        appendLine("|- vcs: $vcsVersion")
        appendLine("|- versions:")
        appendLine(versions.treeView().prepend("| "))
        appendLine("\\- branches:")
        append(nodes.treeView().prepend("  "))
    }

    /**
     * Proxy class for adding nodes to the given branch in the tree.
     *
     * @param id Subproject's name for this branch
     */
    inner class BranchBuilder(private val id: ProjectName) : ProjectProvider {
        /**
         * Buildscript filename override for this branch.
         * Defaults to the one set in the [StonecutterSettings] configuration scope.
         */
        lateinit var buildscript: String

        override fun vers(name: ProjectName, version: TargetVersion) =
            add(id, StonecutterProject(name, version))

        /**
         * Copies nodes registered in [TreeBuilder] to this branch
         */
        fun inherit() = checkNotNull(nodes[""]) {
            "Tried inheriting root nodes before they were registered"
        }.forEach { add(id, it) }
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