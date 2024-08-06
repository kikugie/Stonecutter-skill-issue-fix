package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.StonecutterGradleException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.gradle.api.Project

@Serializable
open class StonecutterSetup(
    val versions: List<StonecutterProject>,
    val vcsVersion: StonecutterProject,
    var current: StonecutterProject = vcsVersion,
) {
    constructor(builder: StonecutterSetupBuilder) : this(
        builder.versions.toList(),
        builder.versionsImpl[builder.vcsVersion]
            ?: throw StonecutterGradleException("Project ${builder.vcsVersion} is not registered")
    )

    @Transient
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