package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.then
import kotlinx.serialization.Serializable
import org.gradle.api.Project

/**
 * Represents a Gradle project path in colon-separated notation.
 * Provided path must be absolute, starting with `:`.
 * @property path String representation of the path
 */
@JvmInline @Serializable
value class ProjectHierarchy(val path: String) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
        require(path.startsWith(':')) { "Path must be absolute" }
    }

    /**All subprojects in this path. If this is the root project, the list will be empty.*/
    val segments
        get() = if (path == ":") emptyList()
        else path.substring(1).split(":")

    /**Whenever this path is the root project.*/
    fun isEmpty(): Boolean = path == ":"

    /**Last subproject entry in the path or `:` if this is the root project.*/
    fun last(): String = lastOrNull() ?: path

    /**Last subproject entry in the path or `null` if this is the root project.*/
    fun lastOrNull(): String? = when (path) {
        ":" -> null
        else -> path.substringAfterLast(':')
    }

    /**Creates a new path, with the [child] attached. The [child] property must not start with `:`.*/
    operator fun plus(child: String) = require(!child.startsWith(":")) then when (path) {
        ":" -> ProjectHierarchy(":$child")
        else -> ProjectHierarchy("$path:$child")
    }

    /**Creates a new path, without the [child] attached. The [child] property must not start with `:`.*/
    operator fun minus(child: String) = require(!child.startsWith(":")) then when {
        !path.endsWith(":$child") -> this
        path == ":$child" -> ROOT
        else -> ProjectHierarchy(path.removeSuffix(":$child"))
    }

    /**Represents the class as the underlying [path].*/
    override fun toString(): String = path

    companion object {
        /**Empty path.*/
        val ROOT = ProjectHierarchy(":")
        /**Converts [Project.getPath] to [ProjectHierarchy].*/
        val Project.hierarchy get() = ProjectHierarchy(path)

        /**
         * Gets the Gradle project for the given [hierarchy].
         * Receiver can be any project, since [ProjectHierarchy] is guaranteed to be an absolute path.
         */
        fun Project.locate(hierarchy: ProjectHierarchy) = project(hierarchy.path)
    }
}

