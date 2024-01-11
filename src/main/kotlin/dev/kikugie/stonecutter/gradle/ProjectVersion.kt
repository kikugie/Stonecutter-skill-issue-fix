package dev.kikugie.stonecutter.gradle

data class ProjectVersion(
    private val plugin: StonecutterBuild,
    private val subProject: SubProject
) {
    val version = subProject.version
    val project = subProject.project
    val isActive: Boolean
        get() = project == plugin.setup.current.project
}