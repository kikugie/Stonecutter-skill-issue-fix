package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.AnyVersion
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.data.tree.NodeModel
import dev.kikugie.stonecutter.data.tree.NodeInfo
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a project entry in a Stonecutter branch.
 */
@Serializable
public data class StonecutterProject internal constructor(
    /**The name of this project's directory, as in `versions/${project}`.*/
    @StonecutterAPI val project: Identifier,
    /**
     * The assigned version of this project. Can be either [SemanticVersion] or [AnyVersion].
     * By default, its equal to [project], unless assigned by using `vers()` in the project settings.
     */
    @StonecutterAPI val version: AnyVersion
) {
    /**
     * Active status of this project.
     *
     * Assigned by [StonecutterController.active], but serialised separately in
     * [NodeModel] and [NodeInfo].
     */
    @Transient
    @StonecutterAPI var isActive: Boolean = false
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
