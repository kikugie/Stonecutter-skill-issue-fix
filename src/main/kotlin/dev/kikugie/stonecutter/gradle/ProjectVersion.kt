package dev.kikugie.stonecutter.gradle

data class ProjectVersion(private val plugin: StonecutterBuild, val version: ProjectName) {
    val isActive: Boolean
        get() = version == plugin.setup.current
}