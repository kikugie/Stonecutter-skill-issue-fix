package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.metadata.StonecutterProject
import dev.kikugie.stonecutter.metadata.TaskName
import org.gradle.api.Project

typealias ProjectPath = String

/**
 * Represents a finalized versions configuration to be passed to [StonecutterController]
 */
open class ProjectSetup(builder: SharedConfigBuilder) {
    internal val versions: List<StonecutterProject> = builder.versions
    internal val vcs: StonecutterProject = builder.vcsVersionImpl
    internal var current: StonecutterProject = vcs

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
        internal fun register(project: ProjectPath, builder: SharedConfigBuilder): Boolean =
            controllers.putIfAbsent(project, ProjectSetup(builder)) == null

        operator fun get(project: Project) = controllers[project.path]
    }
}