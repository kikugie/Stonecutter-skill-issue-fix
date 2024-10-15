@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data.model

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.BuildParameters
import dev.kikugie.stonecutter.data.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

/**
 * Represents the project node saved on the disk.
 * The file is located at `{branch dir}/versions/{version}/build/stonecutter-cache/node.yml`.
 *
 * @property metadata Project data for this node
 * @property root Tree root directory relative to this node
 * @property branch This node's branch. The path is relative to the node's directory
 * @property parameters Parameters used by the file processor
 * @property active Whenever this node is selected as active
 */
@Serializable
data class NodeModel(
    val metadata: StonecutterProject,
    val root: Path,
    val branch: BranchInfo,
    val active: Boolean,
    val parameters: BuildParameters
) {
    /**
     * Node model loader. *This is peak documentation.*
     */
    companion object : ModelLoader<NodeModel> {
        override val filename = "node.yml"
    }
}