package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.ProjectName
import dev.kikugie.stonecutter.metadata.ProjectPath
import dev.kikugie.stonecutter.metadata.TaskName
import org.gradle.api.Action
import org.gradle.api.Project


open class StonecutterConfiguration(
    val versions: List<StonecutterProject>,
    val vcsVersion: StonecutterProject,
    var current: StonecutterProject = vcsVersion,
) {
    constructor(builder: Builder) : this(
        builder.versions.toList(),
        builder.versionsImpl[builder.vcsVersion]
            ?: throw StonecutterGradleException("Project ${builder.vcsVersion} is not registered")
    )

    private val chiseledTasks: MutableSet<TaskName> = mutableSetOf()

    fun register(task: TaskName) {
        chiseledTasks += task
    }

    internal fun anyChiseled(tasks: Iterable<TaskName>): Boolean {
        for (task in tasks) if (task in chiseledTasks) return true
        return false
    }


    open class Builder() {
        constructor(defaults: Builder?, builder: Action<Builder>) : this() {
            defaults?.versionsImpl?.forEach { (k, v) ->
                versionsImpl.putIfAbsent(k, v)
            }
            builder.execute(this)
        }

        private var vcsVersionImpl: ProjectName? = null
        internal var versionsImpl = LinkedHashMap<String, StonecutterProject>()

        var vcsVersion: ProjectName
            get() = vcsVersionImpl ?: versionsImpl.keys.first()
            set(value) {
                vcsVersionImpl = value
            }
        internal val versions: Iterable<StonecutterProject> = versionsImpl.values

        @JvmOverloads
        fun vers(project: ProjectName, version: String = project) {
            versionsImpl[project] = StonecutterProject(project, version)
        }

        fun versions(vararg projects: ProjectName) =
            versions(projects.asIterable())

        fun versions(projects: Iterable<ProjectName>) {
            projects.forEach(::vers)
        }

        internal val vcsProject: StonecutterProject by lazy { versionsImpl[vcsVersion]!! }
    }

    open class Container(
        private val configurations: MutableMap<ProjectPath, StonecutterConfiguration> = mutableMapOf(),
    ) {
        operator fun get(project: Project) = configurations[project.path]
        internal fun register(project: ProjectPath, builder: Builder): Boolean =
            configurations.putIfAbsent(project, StonecutterConfiguration(builder)) == null
    }
}