package dev.kikugie.experimentalstonecutter.data

import dev.kikugie.experimentalstonecutter.controller.ProjectTree
import dev.kikugie.experimentalstonecutter.settings.TreeBuilder
import dev.kikugie.experimentalstonecutter.settings.StonecutterSettings
import dev.kikugie.experimentalstonecutter.controller.StonecutterController
import dev.kikugie.experimentalstonecutter.ProjectPath
import org.gradle.api.Project

/**
 * Storage for trees produced by [StonecutterSettings]
 */
open class TreeBuilderContainer {
    private val projects: MutableMap<ProjectPath, TreeBuilder> = mutableMapOf()
    operator fun get(project: Project): TreeBuilder? = projects[project.path]
    fun register(project: ProjectPath, builder: TreeBuilder): Boolean =
        projects.putIfAbsent(project, builder) == null
}

/**
 * Storage for trees produced by [StonecutterController]
 */
open class TreeContainer {
    private val projects: MutableMap<ProjectPath, ProjectTree> = mutableMapOf()
    operator fun get(project: Project): ProjectTree? = projects[project.path]
    fun register(project: ProjectPath, tree: ProjectTree): Boolean =
        projects.putIfAbsent(project, tree) == null
}