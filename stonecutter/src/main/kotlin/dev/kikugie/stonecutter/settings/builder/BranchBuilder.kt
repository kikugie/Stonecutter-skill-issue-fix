package dev.kikugie.stonecutter.settings.builder

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TargetVersion
import dev.kikugie.stonecutter.settings.ProjectProvider
import dev.kikugie.stonecutter.settings.StonecutterSettings

/**
 * Proxy class for adding nodes to the given branch in the tree.
 *
 * @param id Subproject's name for this branch
 */
class BranchBuilder(private val tree: TreeBuilder, private val id: ProjectName) : ProjectProvider {
    /**
     * Buildscript filename override for this branch.
     * Defaults to the one set in the [StonecutterSettings] configuration scope.
     */
    lateinit var buildscript: String

    override fun vers(
        name: ProjectName,
        version: TargetVersion
    ) = tree.add(id, tree.find(StonecutterProject(name, version)))

    /**
     * Copies nodes registered in [TreeBuilder] to this branch
     */
    fun inherit() = checkNotNull(tree.nodes[""]) {
        "No root node to inherit from"
    }.forEach { tree.add(id, it) }
}