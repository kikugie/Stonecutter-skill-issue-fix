package dev.kikugie.stonecutter

import org.gradle.api.Action

/**
 * Represents the initial setup defined in `settings.gradle` in the `shared` block.
 */
@Suppress("LeakingThis")
open class StonecutterConfigurationBuilder internal constructor() {
    internal constructor(builder: Action<StonecutterConfigurationBuilder>) : this() {
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
            require(value in versionsImpl) { "VCS version must be one of the registered versions!" }
            vcsVersionImpl = value
        }
    internal val versions: Iterable<StonecutterProject> = versionsImpl.values
    internal val vcsProject: StonecutterProject get() = versionsImpl[vcsVersion]!!

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

    /**
     * The base version for this project.
     * It is selected on the first setup and targeted by `Reset active project`
     */
    fun vcsVersion(version: String) {
        vcsVersion = version
    }
}