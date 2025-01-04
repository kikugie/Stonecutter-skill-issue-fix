package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.tree.TreeBuilder
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

/**Method variations for [StonecutterSettings]*/
public abstract class SettingsAbstraction(private val settings: Settings) {
    private lateinit var shared: TreeBuilder
    /**Stores the provided configuration to be used in [create] methods.*/
    @StonecutterAPI public fun shared(action: Action<TreeBuilder>) {
        shared = TreeBuilder().also(action::execute)
    }

    /**Configures the specified project to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(project: ProjectPath) {
        create(project.project(), shared)
    }

    /**Configures the specified project to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(project: ProjectDescriptor) {
        create(project, shared)
    }

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(vararg projects: ProjectPath): Unit =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(vararg projects: ProjectDescriptor): Unit =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectPath>): Unit =
        projects.forEach(::create)

    /**Configures the specified projects to be versioned with setup provided by [shared].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectDescriptor>): String =
        projects.forEach(::create).let { BNAN }

    /**Configures the specified project to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(project: ProjectPath, action: Action<TreeBuilder>): Unit =
        create(project.project(), action)

    /**Configures the specified project to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(project: ProjectDescriptor, action: Action<TreeBuilder>): Unit =
        create(project, TreeBuilder().also(action::execute))

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(vararg projects: ProjectPath, action: Action<TreeBuilder>): Unit =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(vararg projects: ProjectDescriptor, action: Action<TreeBuilder>): Unit =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectPath>, action: Action<TreeBuilder>): Unit =
        projects.forEach { create(it, action) }

    /**Configures the specified projects to be versioned with setup provided by [action].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectDescriptor>, action: Action<TreeBuilder>): String =
        projects.forEach { create(it, action) }.let { BNAN }

    protected abstract fun create(project: ProjectDescriptor, setup: TreeBuilder)

    protected fun ProjectPath.project(): ProjectDescriptor = removeStarting(':').let {
        if (it.isEmpty()) settings.rootProject
        else {
            settings.include(it)
            settings.project(":$it")
        }
    }
}