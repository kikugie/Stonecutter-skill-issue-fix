package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.BNAN
import dev.kikugie.stonecutter.ProjectPath
import dev.kikugie.stonecutter.settings.builder.TreeBuilder
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

/**Method variations for [StonecutterSettings]*/
abstract class SettingsConfiguration(private val settings: Settings) {
    protected lateinit var shared: TreeBuilder
    /**Stores the provided configuration to be used in [create] methods.*/
    fun shared(action: Action<TreeBuilder>) {
        shared = TreeBuilder().also(action::execute)
    }

    /**Configures the specified project to be versioned with setup provided by [shared].*/
    fun create(project: ProjectPath) {
        create(project.project(), shared)
    }

    /**Configures the specified project to be versioned with setup provided by [shared].*/
    fun create(project: ProjectDescriptor) {
        create(project, shared)
    }

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    fun create(vararg projects: ProjectPath) =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    fun create(vararg projects: ProjectDescriptor) =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    fun create(projects: Iterable<ProjectPath>) =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    fun create(projects: Iterable<ProjectDescriptor>) =
        projects.forEach(::create).let { BNAN }

    /**Configures the specified project to be versioned with setup provided by [action].*/
    fun create(project: ProjectPath, action: Action<TreeBuilder>) =
        create(project.project(), action)

    /**Configures the specified project to be versioned with setup provided by [action].*/
    fun create(project: ProjectDescriptor, action: Action<TreeBuilder>) =
        create(project, TreeBuilder().also(action::execute))

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    fun create(vararg projects: ProjectPath, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    fun create(vararg projects: ProjectDescriptor, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    fun create(projects: Iterable<ProjectPath>, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    fun create(projects: Iterable<ProjectDescriptor>, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }.let { BNAN }

    protected abstract fun create(project: ProjectDescriptor, setup: TreeBuilder)

    protected fun ProjectPath.project(): ProjectDescriptor = with(this.removePrefix(":")) {
        if (isEmpty()) settings.rootProject
        else {
            settings.include(this)
            settings.project(":$this")
        }
    }
}