package dev.kikugie.stonecutter

import org.gradle.api.Project


internal open class StonecutterSetup(
    val versions: List<StonecutterProject>,
    val vcsVersion: StonecutterProject,
    var current: StonecutterProject = vcsVersion,
) {
    constructor(builder: StonecutterSetupBuilder) : this(
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

    internal open class Container(
        private val configurations: MutableMap<ProjectPath, StonecutterSetup> = mutableMapOf(),
    ) {
        operator fun get(project: Project) = configurations[project.path]
        internal fun register(project: ProjectPath, builder: StonecutterSetupBuilder): Boolean =
            configurations.putIfAbsent(project, StonecutterSetup(builder)) == null
    }
}