package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.ProjectPath
import dev.kikugie.stonecutter.data.tree.TreeBuilder
import org.gradle.kotlin.dsl.create
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.getByType

internal inline fun <reified T : ProjectContainerExtension<out Any>> Gradle.createContainer(): T =
    extensions.create<T>(requireNotNull(T::class.simpleName) { "Provided class has no name" })
internal inline fun <reified T : ProjectContainerExtension<out Any>> Gradle.getContainer(): T =
    extensions.getByType<T>()

internal abstract class ProjectContainerExtension<T> {
    private val projects: MutableMap<ProjectPath, T> = mutableMapOf()
    operator fun get(path: ProjectPath): T? = projects[path]
    operator fun get(project: Project): T? = projects[project.path]
    fun register(path: ProjectPath, value: T): Boolean =
        projects.putIfAbsent(path, value) == null
}

internal open class TreeBuilderContainer : ProjectContainerExtension<TreeBuilder>()