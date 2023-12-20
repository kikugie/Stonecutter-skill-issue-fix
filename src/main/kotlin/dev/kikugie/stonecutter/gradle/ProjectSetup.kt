package dev.kikugie.stonecutter.gradle

import org.gradle.api.Project

typealias ProjectPath = String

/**
 * Represents a finalized versions configuration to be passed to [StonecutterController]
 */
@Suppress("unused")
open class ProjectSetup(builder: ProjectBuilder) {
    internal val versions: List<ProjectName> = builder.versions
    internal val vcs: ProjectName = builder.vcsVersion ?: versions.first()
    internal var current: ProjectName = vcs

    private val chiseledTasks: MutableSet<TaskName> = mutableSetOf()
    var debug = false
    var syncProperties = false

    fun register(task: TaskName) {
        chiseledTasks += task
    }

    fun anyChiseled(tasks: Iterable<TaskName>): Boolean {
        for (task in tasks) if (task in chiseledTasks) return true
        return false
    }

    open class SetupContainer(
        private val controllers: MutableMap<ProjectPath, ProjectSetup> = mutableMapOf()
    ) {
        internal fun register(project: ProjectPath, builder: ProjectBuilder): Boolean =
            controllers.putIfAbsent(project, ProjectSetup(builder)) == null

        operator fun get(project: Project) = controllers[project.path]
    }
}