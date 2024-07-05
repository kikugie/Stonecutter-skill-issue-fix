package dev.kikugie.stonecutter

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable

@Serializable
class StonecutterProject internal constructor(
    /**
     * The name of this project's directory, as in `versions/${project}`.
     */
    val project: ProjectName,
    /**
     * The assigned version of this project. Must be a valid [SemanticVersion].
     * By default, its equal to [project], unless assigned by using `vers()` in the project settings.
     */
    val version: TargetVersion
) {
    /**
     * Whenever this project is selected as active.
     */
    var isActive: Boolean = false
        internal set

    internal fun asActive() = this.also { isActive = true }

    override fun equals(other: Any?): Boolean =
        if (other !is StonecutterProject) false
        else project == other.project && version == other.version

    override fun hashCode(): Int {
        var result = project.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }
}
