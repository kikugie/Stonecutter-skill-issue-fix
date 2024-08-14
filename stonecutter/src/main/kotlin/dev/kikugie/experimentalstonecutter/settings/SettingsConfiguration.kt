package dev.kikugie.experimentalstonecutter.settings

import dev.kikugie.stonecutter.BNAN
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor

/**
 * Method variations for [StonecutterSettings]
 */
interface SettingsConfiguration {
    /**
     * Stores the provided configuration to be used in [create] methods.
     *
     * @param action Configuration scope
     */
    fun shared(action: Action<TreeBuilder>)

    /**
     * Configures the specified project to be versioned with setup provided by [shared].
     *
     * @param project Project path. `:` prefix is optional
     */
    fun create(project: String)

    /**
     * Configures the specified project to be versioned with setup provided by [shared].
     *
     * @param project Project reference
     */
    fun create(project: ProjectDescriptor)

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
     * @return 🍌 to prevent JVM signature crash. Do whatever you want with it
     */
    fun create(projects: Iterable<ProjectDescriptor>) =
        projects.forEach(::create).let { BNAN }

    /**
     * Configures the specified project to be versioned with the given setup.
     *
     * @param project Project path. `:` prefix is optional
     * @param action Configuration scope
     */
    fun create(project: String, action: Action<TreeBuilder>)

    /**
     * Configures the specified project to be versioned with the given setup.
     *
     * @param project Project reference
     * @param action Configuration scope
     */
    fun create(project: ProjectDescriptor, action: Action<TreeBuilder>)

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
     * @return 🍌 to prevent JVM signature crash due to type erasure. Do whatever you want with it
     */
    fun create(projects: Iterable<ProjectDescriptor>, action: Action<TreeBuilder>) =
        projects.forEach { create(it, action) }.let { BNAN }
}