package dev.kikugie.stonecutter.settings.builder

import dev.kikugie.stonecutter.AnyVersion
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.settings.ProjectProvider
import dev.kikugie.stonecutter.settings.StonecutterSettings

/**
 * Proxy class for adding nodes to the given branch in the tree.
 *
 * @param id Subproject's name for this branch
 */
class BranchBuilder internal constructor(private val tree: TreeBuilder, private val id: Identifier) : ProjectProvider {
    /**
     * Buildscript filename override for this branch.
     * Defaults to [StonecutterSettings.centralScript].
     */
    lateinit var buildscript: String

    override fun vers(name: Identifier, version: AnyVersion) =
        tree.add(id, StonecutterProject(name, version))

    /**
     * Copies nodes registered in [TreeBuilder] to this branch
     */
    fun inherit() = tree.nodes[""]?.forEach { tree.add(id, it) }
        ?: error("No root node to inherit from")
}