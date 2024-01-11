package dev.kikugie.stonecutter.gradle

import org.gradle.api.Project

typealias ProjectPath = String

/**
 * Represents a finalized versions configuration to be passed to [StonecutterController]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ProjectSetup(builder: ProjectBuilder) {
    internal val versions: List<SubProject> = builder.versions
    internal val vcs: SubProject = builder.vcsVersion
    internal var current: SubProject = vcs

    private val chiseledTasks: MutableSet<TaskName> = mutableSetOf()
    var debug = false

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