package dev.kikugie.stonecutter.metadata

data class StonecutterProject(
    val project: ProjectName,
    val version: TargetVersion,
    val isActive: Boolean = false
) {

    fun asActive() = StonecutterProject(project, version, true)
}

