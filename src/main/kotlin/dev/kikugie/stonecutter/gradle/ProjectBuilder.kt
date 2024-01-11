package dev.kikugie.stonecutter.gradle

import org.gradle.api.Action
import org.gradle.api.GradleException
import java.util.*

/**
 * Represents the initial setup defined in `settings.gradle` in the `shared` block.
 * @see StonecutterSettings
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ProjectBuilder() {
    private var _vcsVersion: ProjectName? = null
    private var _versions = LinkedHashMap<ProjectName, SubProject>()

    internal val vcsVersion: SubProject by lazy {
        if (_vcsVersion == null) versions.first()
        else _versions[_vcsVersion]
            ?: throw GradleException("[Stonecutter] Project $_vcsVersion is not registered")
    }
    internal val versions: List<SubProject> by lazy(_versions.values::toList)

    constructor(defaults: ProjectBuilder?, builder: Action<ProjectBuilder>) : this() {
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
            throw GradleException("[Stonecutter] Project $project is already registered")
        this._versions[project] = (SubProject(project, version))
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
    fun vcsVersion(version: ProjectName) {
        _vcsVersion = version
    }

    companion object {
        val DEFAULT: ProjectBuilder = ProjectBuilder()
    }
}