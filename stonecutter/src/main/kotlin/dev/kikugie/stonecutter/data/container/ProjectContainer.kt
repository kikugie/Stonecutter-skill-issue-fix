package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.ProjectPath
import org.gradle.api.Project

/**
 * Common storage for data to be shared between versioned buildscripts.
 *
 * @param T Storage type
 */
abstract class ProjectContainer<T> {
    private val projects: MutableMap<ProjectPath, T> = mutableMapOf()

    /**
     * Retrieves the value associated with the given project from the storage.
     *
     * @param project The project for which to retrieve the value
     * @return The value associated with the project, or `null` if none exists
     */
    operator fun get(project: Project): T? = projects[project.path]

    /**
     * Registers a project with a value in the storage.
     *
     * @param project The project path to register
     * @param value The value to associate with the project
     * @return `true` if the project was registered successfully, `false` if the project is already registered
     */
    fun register(project: ProjectPath, value: T): Boolean =
        projects.putIfAbsent(project, value) == null

    /**
     * Registers a project with a value in the storage.
     *
     * @param project The project to register
     * @param value The value to associate with the project
     * @return `true` if the project was registered successfully, `false` if the project is already registered
     */
    fun register(project: Project, value: T): Boolean =
        register(project.path, value)
}