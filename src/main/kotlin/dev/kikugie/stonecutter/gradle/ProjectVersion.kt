package dev.kikugie.stonecutter.gradle

class ProjectVersion(private val plugin: StonecutterBuild, internal val version: ProjectName) {
    val active = version == plugin.setup.current
    val project = plugin.project
}