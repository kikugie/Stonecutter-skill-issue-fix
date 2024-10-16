package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.data.model.NodeModel
import dev.kikugie.stonecutter.data.model.NodeInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a project entry in a Stonecutter branch.
 */
@Serializable
data class StonecutterProject internal constructor(
    /**The name of this project's directory, as in `versions/${project}`.*/
    val project: Identifier,
    /**
     * The assigned version of this project. Can be either [SemanticVersion] or [AnyVersion].
     * By default, its equal to [project], unless assigned by using `vers()` in the project settings.
     */
    val version: AnyVersion
) {
    /**
     * Active status of this project.
     *
     * Assigned by [StonecutterController.active], but serialized separately in
     * [NodeModel] and [NodeInfo].
     */
    @Transient
    var isActive: Boolean = false
        internal set

    init {
        require(project.isNotBlank()) { "Project must not be blank" }
        require(version.isNotBlank()) { "Version must not be blank" }
        require(project.isValid()) { "Invalid project name: '$project'" }
    }

    /**Checks if the versions are equal, excluding the [isActive] parameter.*/
    override fun equals(other: Any?): Boolean =
        if (other !is StonecutterProject) false
        else project == other.project && version == other.version

    /**Calculates the hash code, excluding the [isActive] parameter.*/
    override fun hashCode(): Int {
        var result = project.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }
}
