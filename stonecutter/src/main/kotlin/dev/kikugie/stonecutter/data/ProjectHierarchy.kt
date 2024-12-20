package dev.kikugie.stonecutter.data

import kotlinx.serialization.Serializable
import org.gradle.api.Project

@JvmInline @Serializable
value class ProjectHierarchy(val path: String) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
        require(path.startsWith(':')) { "Path must be absolute" }
    }

    val segments
        get() = if (path == ":") emptyList()
        else path.substring(1).split(":")
    val size: Int
        get() = if (path == ":") 0
        else path.count { it == ':' } + 1

    fun isEmpty(): Boolean = path == ":"

    fun last(): String = lastOrNull() ?: throw NoSuchElementException("Empty path")
    fun lastOrThis(): String = lastOrNull() ?: path
    fun lastOrNull(): String? = when (path) {
        ":" -> null
        else -> path.substringAfterLast(':')
    }

    operator fun plus(child: String) = when (path) {
        ":" -> ProjectHierarchy(":$child")
        else -> ProjectHierarchy("$path:$child")
    }

    operator fun minus(child: String) =
        if (!path.endsWith(":$child")) this
        else ProjectHierarchy(path.removeSuffix(":$child"))

    operator fun get(index: Int): String = when (path) {
        ":" -> throw IndexOutOfBoundsException("Empty path")
        else -> segments[index]
    }

    override fun toString(): String = path

    companion object {
        val ROOT = ProjectHierarchy(":")
        val Project.hierarchy get() = ProjectHierarchy(path)
        fun Project.locate(hierarchy: ProjectHierarchy) = project(hierarchy.path)
    }
}

