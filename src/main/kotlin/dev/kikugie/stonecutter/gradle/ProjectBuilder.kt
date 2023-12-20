package dev.kikugie.stonecutter.gradle

import org.gradle.api.Action
import java.util.*

/**
 * Represents the inital setup defined in `settings.gradle` in the `shared` block.
 * @see StonecutterSettings
 */
@Suppress("unused")
class ProjectBuilder() {
    internal var vcsVersion: ProjectName? = null
    internal var versions: List<ProjectName> = emptyList()

    constructor(defaults: ProjectBuilder?, builder: Action<ProjectBuilder>) : this() {
        versions = defaults?.versions ?: LinkedList()
        builder.execute(this)
    }

    /**
     * Specifies subprojects and Minecraft versions used for the comment processor.
     *
     * @param versions version names in semantic format
     */
    fun versions(vararg versions: ProjectName) {
        if (versions.isEmpty() || versions.toSet().size != versions.size)
            throw IllegalArgumentException("Invalid versions: $versions")

        this.versions = versions.toList()
    }

    /**
     * Specifies initial active version. By default selects first one from the subprojects list.
     *
     * @param version subproject name
     */
    fun vcsVersion(version: ProjectName) {
        vcsVersion = version
    }

    companion object {
        val DEFAULT: ProjectBuilder = ProjectBuilder()
    }
}