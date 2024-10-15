@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data.model

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.controller.storage.ProjectNode
import dev.kikugie.stonecutter.data.PathSerializer
import kotlinx.serialization.*
import java.nio.file.Path

/**
 * Information about a tree node.
 *
 * @property metadata Project data for this node
 * @property path Contextual path to this node. Refer to the `@see` section below
 * @property active Whenever this node is selected as active
 * @see TreeModel.nodes
 * @see BranchModel.nodes
 */
@Serializable
data class NodeInfo(
    val metadata: StonecutterProject,
    val path: Path,
    val active: Boolean,
) {
    internal companion object {
        fun ProjectNode.toNodeInfo(source: Path, active: StonecutterProject) = NodeInfo(metadata, source, metadata == active)
    }
}