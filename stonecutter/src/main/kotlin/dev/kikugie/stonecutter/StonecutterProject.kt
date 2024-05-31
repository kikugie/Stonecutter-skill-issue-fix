package dev.kikugie.stonecutter

data class StonecutterProject internal constructor(
    val project: ProjectName,
    val version: TargetVersion,
    val isActive: Boolean = false,
) {
    internal fun asActive() = StonecutterProject(project, version, true)
}
