package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.ProjectName
import dev.kikugie.stonecutter.metadata.StonecutterProject
import org.gradle.api.Action
import java.util.*

/**
 * Represents the initial setup defined in `settings.gradle` in the `shared` block.
 * @see StonecutterSettings
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class SharedConfigBuilder() {
    var vcsVersion: ProjectName? = null
    private var _versions = LinkedHashMap<ProjectName, StonecutterProject>()

    internal val vcsVersionImpl: StonecutterProject by lazy {
        if (vcsVersion == null) versions.first()
        else _versions[vcsVersion]
            ?: throw StonecutterGradleException("Project $vcsVersion is not registered") {
                """
                    // Example
                    shared {
                        versions("1.19.4", "1.20.1")
                        vcsVersion = "1.20.2" // Wrong!
                        vcsVersion = "1.20.1" // Ok
                    }
                """.trimIndent()
            }
    }
    internal val versions: List<StonecutterProject> by lazy(_versions.values::toList)

    constructor(defaults: SharedConfigBuilder?, builder: Action<SharedConfigBuilder>) : this() {
        defaults?._versions?.forEach { (k, v) ->
            _versions.putIfAbsent(k, v)
        }
        builder.execute(this)
    }

    /**
     * Creates a subproject with separate directory and Minecraft version.
     *
     * @param project subproject directory to be used in `./versions/`. Can't be a duplicate.
     * @param version Minecraft version assigned to this subproject. Can be assigned to multiple subprojects.
     */
    fun vers(project: ProjectName, version: String) {
        if (project in _versions)
            throw StonecutterGradleException("[Stonecutter] Project $project is already registered") {
                """
                    // Example
                    shared {
                        versions("1.19.4", "1.20.1", "1.20.2", "1.19.4") // Wrong!
                        //          ^- - - - - duplicates - - - - -^
                    }   
                """.trimIndent()
            }
        this._versions[project] = (StonecutterProject(project, version))
    }

    // Duplicate methods because groovy doesn't have array unpacking :skull:
    /**
     * Adds subprojects to the Stonecutter setup.
     * Subproject directory and version are set to the same value.
     * For more precise controls use [vers].
     *
     * @param projects subprojects to be included.
     */
    fun versions(vararg projects: ProjectName) = versions(projects.toList())

    /**
     * Adds subprojects to the Stonecutter setup.
     * Subproject directory and version are set to the same value.
     * For more precise controls use [vers].
     *
     * @param projects subprojects to be included.
     */
    fun versions(projects: Collection<ProjectName>) {
        projects.forEach { vers(it, it) }
    }

    /**
     * Specifies initial active version. By default, selects first one from the subprojects list.
     *
     * @param version subproject name
     */
    @Deprecated(
        message = "This method is deprecated, use property setter instead",
        replaceWith = ReplaceWith("vcsVersion = value")
    )
    fun vcsVersion(version: ProjectName) {
        vcsVersion = version
    }

    companion object {
        val DEFAULT: SharedConfigBuilder = SharedConfigBuilder()
    }
}