@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data.model

import dev.kikugie.stonecutter.controller.storage.ProjectBranch
import dev.kikugie.stonecutter.data.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

/**
 * Information about a tree branch.
 *
 * @property id ID of the branch equivalent to the Gradle subproject name
 * @property path Contextual path to this branch. Refer to the `@see` section below
 * @see TreeModel.branches
 * @see NodeModel.branch
 */
@Serializable
data class BranchInfo(
    val id: String,
    val path: Path,
) {
    internal companion object {
        fun ProjectBranch.toBranchInfo(source: Path) = BranchInfo(id, source)
    }
}