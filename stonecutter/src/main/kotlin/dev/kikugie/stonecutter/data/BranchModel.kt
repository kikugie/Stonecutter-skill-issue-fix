@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.StonecutterProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

/**
 * Represents the project branch saved on the disk.
 * The file is located at `{branch dir}/build/stonecutter-cache/branch.yml`.
 * For the root branch it will be in the same directory as `tree.yml`
 *
 * @property id Name of this branch
 * @property root Tree root directory relative to this branch
 * @property nodes List of nodes in this branch. Each node's path is relative to the branch's directory
 */
@Serializable
data class BranchModel(
    val id: String,
    val root: Path,
    val nodes: List<NodeInfo>,
) {
    /**
     * Versions in this branch. This is equivalent to [ProjectBranch.versions].
     */
    @Transient
    val versions: Set<StonecutterProject> = nodes.map { it.metadata }.toSet()

    /**
     * Branch model loader. *This is peak documentation.*
     */
    companion object : ModelLoader<BranchModel> {
        override val filename = "branch.yml"
    }
}