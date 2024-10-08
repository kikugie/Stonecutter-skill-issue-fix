@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.storage.GlobalParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

/**
 * Represents the project tree saved on the disk.
 * The file is located at `{tree root}/build/stonecutter-cache/tree.yml`.
 *
 * @property vcs Version control project. `Reset active project` task targets this
 * @property current The currently active project
 * @property branches List of registered branches. Each branch's path is relative to this project's directory
 * @property nodes List of all nodes. Each node's path is relative to this project's directory
 * @property parameters Global parameters for this project tree
 */
@Serializable
data class TreeModel(
    val vcs: StonecutterProject,
    val current: StonecutterProject,
    val branches: List<BranchInfo>,
    val nodes: List<NodeInfo>,
    val parameters: GlobalParameters,
) {
    /**
     * Common versions across all nodes. This is equivalent to [ProjectTree.versions].
     */
    @Transient
    val versions: Set<StonecutterProject> = nodes.map { it.metadata }.toSet()

    /**
     * Tree model loader. *This is peak documentation.*
     */
    companion object : ModelLoader<TreeModel> {
        override val filename = "tree.yml"
    }
}