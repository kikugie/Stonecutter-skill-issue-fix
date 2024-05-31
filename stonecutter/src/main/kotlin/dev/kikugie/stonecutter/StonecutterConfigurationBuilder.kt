package dev.kikugie.stonecutter

import org.gradle.api.Action

/**
 * Represents the initial setup defined in `settings.gradle` in the `shared` block.
 */
@Suppress("LeakingThis")
open class StonecutterConfigurationBuilder internal constructor() {
    internal constructor(defaults: StonecutterConfigurationBuilder?, builder: Action<StonecutterConfigurationBuilder>) : this() {
        defaults?.versionsImpl?.forEach { (k, v) ->
            versionsImpl.putIfAbsent(k, v)
        }
        builder.execute(this)
    }

    private var vcsVersionImpl: ProjectName? = null
    internal var versionsImpl = LinkedHashMap<String, StonecutterProject>()

    /**
     * The base version for this project.
     * It is selected on the first setup and targeted by `Reset active project`
     */
    var vcsVersion: ProjectName
        get() = vcsVersionImpl ?: versionsImpl.keys.first()
        set(value) {
            vcsVersionImpl = value
        }
    internal val versions: Iterable<StonecutterProject> = versionsImpl.values

    /**
     * Creates a subproject with separate directory and Minecraft version.
     *
     * @param project subproject directory to be used in `./versions/`. Can't be a duplicate
     * @param version Minecraft version assigned to this subproject. Can be assigned to multiple subprojects
     */
    @JvmOverloads
    fun vers(project: ProjectName, version: String = project) {
        versionsImpl[project] = StonecutterProject(project, version)
    }

    /**
     * Adds subprojects to the Stonecutter setup.
     * Subproject directory and version are set to the same value.
     * For more precise controls use [vers].
     *
     * @param projects subprojects to be included
     */
    fun versions(vararg projects: ProjectName) =
        versions(projects.asIterable())

    /**
     * Adds subprojects to the Stonecutter setup.
     * Subproject directory and version are set to the same value.
     * For more precise controls use [vers].
     *
     * @param projects subprojects to be included
     */
    fun versions(projects: Iterable<ProjectName>) {
        projects.forEach(::vers)
    }

    internal val vcsProject: StonecutterProject by lazy { versionsImpl[vcsVersion]!! }
}