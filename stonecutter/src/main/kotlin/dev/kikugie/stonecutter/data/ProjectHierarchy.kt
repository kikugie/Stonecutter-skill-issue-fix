package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.then
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.UnknownProjectException

/**
 * Represents a Gradle project path in colon-separated notation.
 * Provided path must be absolute, starting with `:`.
 * @property path String representation of the path
 */
@JvmInline @Serializable
public value class ProjectHierarchy(private val path: String) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
        require(path.startsWith(':')) { "Path must be absolute" }
    }

    /**All subprojects in this path. If this is the root project, the list will be empty.*/
    public val segments: List<String>
        get() = if (path == ":") emptyList()
        else path.substring(1).split(":")

    public fun orBlank(): String = if (isEmpty()) "" else path

    /**Whenever this path is the root project.*/
    public fun isEmpty(): Boolean = path == ":"

    /**Last subproject entry in the path or empty string if this is the root project.*/
    public fun last(): String = lastOrNull() ?: ""

    /**Last subproject entry in the path or `null` if this is the root project.*/
    private fun lastOrNull(): String? = when (path) {
        ":" -> null
        else -> path.substringAfterLast(':')
    }

    /**Creates a new path, with the [child] attached. The [child] property must not start with `:`.*/
    public operator fun plus(child: String): ProjectHierarchy = require(!child.startsWith(":")) then when (path) {
        ":" -> ProjectHierarchy(":$child")
        else -> when(child) {
            "" -> this
            else -> ProjectHierarchy("$path:$child")}
    }

    /**Creates a new path, without the [child] attached. The [child] property must not start with `:`.*/
    public operator fun minus(child: String): ProjectHierarchy = require(!child.startsWith(":")) then when {
        !path.endsWith(":$child") -> this
        path == ":$child" -> ROOT
        else -> ProjectHierarchy(path.removeSuffix(":$child"))
    }

    /**Represents the class as the underlying [path].*/
    override fun toString(): String = path

    public companion object {
        /**Empty path.*/
        public val ROOT: ProjectHierarchy = ProjectHierarchy(":")
        /**Converts [Project.getPath] to [ProjectHierarchy].*/
        public val Project.hierarchy: ProjectHierarchy get() = ProjectHierarchy(path)

        /**
         * Gets the Gradle project for the given [hierarchy].
         * Receiver can be any project, since [ProjectHierarchy] is guaranteed to be an absolute path.
         * @throws UnknownProjectException if the path is invalid
         */
        public fun Project.locate(hierarchy: ProjectHierarchy): Project = project(hierarchy.path)
    }
}

