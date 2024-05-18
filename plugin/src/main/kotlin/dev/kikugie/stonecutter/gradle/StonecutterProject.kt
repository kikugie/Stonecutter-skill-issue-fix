package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.ProjectName
import dev.kikugie.stonecutter.metadata.TargetVersion

data class StonecutterProject internal constructor(
    val project: ProjectName,
    val version: TargetVersion,
    val isActive: Boolean = false,
) {
    internal fun asActive() = StonecutterProject(project, version, true)
}
