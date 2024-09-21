package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.BNAN
import dev.kikugie.stonecutter.ProjectPath
import dev.kikugie.stonecutter.sanitize
import dev.kikugie.stonecutter.settings.builder.TreeBuilder
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

/**
 * Method variations for [StonecutterSettings]
 */
abstract class SettingsConfiguration(private val settings: Settings) {
    protected lateinit var shared: TreeBuilder
    /**
     * Stores the provided configuration to be used in [create] methods.
     *
     * @param action Configuration scope
     */
    fun shared(action: Action<TreeBuilder>) {
        shared = TreeBuilder().also(action::execute)
    }

    /**
     * Configures the specified project to be versioned with setup provided by [shared].
     *
     * @param project Project path. `:` prefix is optional
     */
    fun create(project: String) {
        create(get(project), shared)
    }

    /**
     * Configures the specified project to be versioned with setup provided by [shared].
     *
     * @param project Project reference
     */
    fun create(project: ProjectDescriptor) {
        create(project, shared)
    }

    /**
     * Configures the specified projects to be versioned with setup provided by [shared].
     *
     * @param projects Project paths. `:` prefix is optional
     */
    fun create(vararg projects: String) =
        projects.forEach(::create)

    /**
     * Configures the specified projects to be versioned with setup provided by [shared].
     *
     * @param projects Project references
     */
    fun create(vararg projects: ProjectDescriptor) =
        projects.forEach(::create)

    /**
     * Configures the specified projects to be versioned with setup provided by [shared].
     *
     * @param projects Project paths. `:` prefix is optional
     */
    fun create(projects: Iterable<String>) =
        projects.forEach(::create)

    /**
     * Configures the specified projects to be versioned with setup provided by [shared].
     *
     * @param projects Project references
     */
    fun create(projects: Iterable<ProjectDescriptor>) =
        projects.forEach(::create).let { BNAN }

    /**
     * Configures the specified project to be versioned with the given setup.
     *
     * @param project Project path. `:` prefix is optional
     * @param action Configuration scope
     */
    fun create(project: String, action: Action<TreeBuilder>) {
        create(get(project), action)
    }

    /**
     * Configures the specified project to be versioned with the given setup.
     *
     * @param project Project reference
     * @param action Configuration scope
     */
    fun create(project: ProjectDescriptor, action: Action<TreeBuilder>) {
        create(project, TreeBuilder().also(action::execute))
    }

    /**
     * Configures the specified projects to be versioned with the given setup.
     *
     * @param projects Project paths. `:` prefix is optional
     * @param action Configuration scope
     */
    fun create(vararg projects: String, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**
     * Configures the specified projects to be versioned with the given setup.
     *
     * @param projects Project references
     * @param action Configuration scope
     */
    fun create(vararg projects: ProjectDescriptor, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**
     * Configures the specified projects to be versioned with the given setup.
     *
     * @param projects Project paths. `:` prefix is optional
     * @param action Configuration scope
     */
    fun create(projects: Iterable<String>, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }

    /**
     * Configures the specified projects to be versioned with the given setup.
     *
     * @param projects Project references
     * @param action Configuration scope
     */
    fun create(projects: Iterable<ProjectDescriptor>, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }.let { BNAN }

    protected abstract fun create(project: ProjectDescriptor, setup: TreeBuilder)

    protected fun get(path: ProjectPath): ProjectDescriptor = with(path.sanitize()) {
        if (isEmpty()) settings.rootProject
        else {
            settings.include(this)
            settings.project(":$this")
        }
    }
}