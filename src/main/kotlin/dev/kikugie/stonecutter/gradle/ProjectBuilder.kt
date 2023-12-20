package dev.kikugie.stonecutter.gradle

import org.gradle.api.Action
import java.util.*

/**
 * Represents the inital setup defined in `settings.gradle` in the `shared` block.
 * @see StonecutterSettings
 */
class ProjectBuilder() {
    internal var vcsVersion: ProjectName? = null
    internal lateinit var versions: List<ProjectName>

    constructor(defaults: ProjectBuilder?, builder: Action<ProjectBuilder>) : this() {
        versions = defaults?.versions ?: LinkedList()
        builder.execute(this)
    }

    fun versions(vararg versions: ProjectName) {
        if (versions.isEmpty() || versions.toSet().size != versions.size)
            throw IllegalArgumentException("Invalid versions: $versions")

        this.versions = versions.toList()
    }

    fun vcsVersion(version: ProjectName) {
        vcsVersion = version
    }

    companion object {
        val DEFAULT: ProjectBuilder = ProjectBuilder()
    }
}