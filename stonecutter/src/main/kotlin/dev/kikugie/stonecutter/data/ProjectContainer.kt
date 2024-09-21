package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.ProjectPath
import org.gradle.api.Project

abstract class ProjectContainer<T> {
    private val projects: MutableMap<ProjectPath, T> = mutableMapOf()
    operator fun get(project: Project): T? = projects[project.path]

    fun register(project: ProjectPath, value: T): Boolean =
        projects.putIfAbsent(project, value) == null

    fun register(project: Project, value: T): Boolean =
        register(project.path, value)
}