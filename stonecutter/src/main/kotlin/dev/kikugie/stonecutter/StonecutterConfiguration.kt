package dev.kikugie.stonecutter

import org.gradle.api.Project


internal open class StonecutterConfiguration(
    val versions: List<StonecutterProject>,
    val vcsVersion: StonecutterProject,
    var current: StonecutterProject = vcsVersion,
) {
    constructor(builder: StonecutterConfigurationBuilder) : this(
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
        private val configurations: MutableMap<ProjectPath, StonecutterConfiguration> = mutableMapOf(),
    ) {
        operator fun get(project: Project) = configurations[project.path]
        internal fun register(project: ProjectPath, builder: StonecutterConfigurationBuilder): Boolean =
            configurations.putIfAbsent(project, StonecutterConfiguration(builder)) == null
    }
}