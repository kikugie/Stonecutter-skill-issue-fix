package dev.kikugie.experimentalstonecutter.data

import dev.kikugie.experimentalstonecutter.controller.ProjectTree
import dev.kikugie.experimentalstonecutter.settings.TreeBuilder
import dev.kikugie.experimentalstonecutter.ProjectPath
import org.gradle.api.Project

internal open class TreeModelContainer {
    internal val projects: MutableMap<ProjectPath, TreeModel> = mutableMapOf()
    operator fun get(project: Project): TreeModel? = projects[project.path]
    fun register(project: ProjectPath, builder: TreeBuilder): Boolean =
        projects.putIfAbsent(project, TreeModel(builder.nodes, builder.versions, builder.vcsProject)) == null
}

internal open class TreeContainer {
    private val projects: MutableMap<ProjectPath, ProjectTree> = mutableMapOf()
    operator fun get(project: Project): ProjectTree? = projects[project.path]
    fun register(project: ProjectPath, tree: ProjectTree): Boolean =
        projects.putIfAbsent(project, tree) == null
}