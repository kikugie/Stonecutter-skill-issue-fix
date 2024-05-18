package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.ProjectName
import dev.kikugie.stonecutter.metadata.TargetVersion

data class StonecutterProject(
    val project: ProjectName,
    val version: TargetVersion,
    val isActive: Boolean = false,
) {
    fun asActive() = StonecutterProject(project, version, true)
}
