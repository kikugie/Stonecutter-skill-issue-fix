@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.storage.ProjectNode
import kotlinx.serialization.*
import java.nio.file.Path

/**
 * Information about a tree node.
 *
 * @property metadata Project data for this node
 * @property path Contextual path to this node. Refer to the `@see` section below
 * @see TreeModel.nodes
 * @see BranchModel.nodes
 */
@Serializable
data class NodeInfo(
    val metadata: StonecutterProject,
    val path: Path,
) {
    internal companion object {
        fun ProjectNode.toNodeInfo(source: Path) = NodeInfo(metadata, source)
    }
}