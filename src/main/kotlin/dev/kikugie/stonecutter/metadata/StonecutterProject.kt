package dev.kikugie.stonecutter.metadata

import dev.kikugie.stonecutter.gradle.StonecutterBuild

class StonecutterProject(
    val project: ProjectName,
    val version: TargetVersion,
    plugin: StonecutterBuild? = null
) {
    val isActive = project == plugin?.setup?.current?.project

    fun withPlugin(plugin: StonecutterBuild) = StonecutterProject(project, version, plugin)
}

