@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data.model

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.storage.GlobalParameters
import dev.kikugie.stonecutter.controller.storage.ProjectTree
import dev.kikugie.stonecutter.data.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

/**
 * Represents the project tree saved on the disk.
 * The file is located at `{tree root}/build/stonecutter-cache/tree.yml`.
 *
 * @property stonecutter Stonecutter version used when writing the file
 * @property vcs Version control project. `Reset active project` task targets this
 * @property current The currently active project
 * @property branches List of registered branches. Each branch's path is relative to this project's directory
 * @property nodes List of all nodes. Each node's path is relative to this project's directory
 * @property parameters Global parameters for this project tree
 */
@Serializable
data class TreeModel(
    val stonecutter: String,
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

    /**Saves the model to [FILE] in the given [directory] in the YAML format.*/
    fun save(directory: Path) = ModelLoader.save(directory.resolve(FILE), this, serializer())

    companion object {
        /**Filename used to save and load the model*/
        const val FILE = "tree.yml"
        /**Loads model from the [FILE] in the given [directory].*/
        @JvmStatic fun load(directory: Path): Result<TreeModel> = ModelLoader.load(directory.resolve(FILE), serializer())
    }
}