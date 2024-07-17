package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterSetupBuilder
import dev.kikugie.stonecutter.StonecutterSettings
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor

/**
 * Method variations for [StonecutterSettings]
 */
interface StonecutterInitialization {
    /**
     * Stores the version structure
     *
     * @param builder Configuration scope
     */
    fun shared(builder: Action<StonecutterSetupBuilder>)

    /**
     * Makes the specified project versioned,
     * with configuration specified by the [shared] block.
     *
     * @param project Project reference
     */
    fun create(project: ProjectDescriptor)

    /**
     * Makes the specified project versioned
     * and assigns the provided configuration to it.
     *
     * @param project Project reference
     * @param setup Configuration instance
     */
    fun create(project: ProjectDescriptor, setup: StonecutterSetupBuilder)

    /**
     * Includes the provided project and makes it versioned,
     * with configuration specified by the [shared] block.
     *
     * @param project Project path
     */
    fun create(project: String)

    /**
     * Includes the provided project, makes it versioned
     * and assigns the provided configuration to it.
     *
     * @param project Project path
     * @param setup Configuration instance
     */
    fun create(project: String, setup: StonecutterSetupBuilder)

    /**
     * Makes the specified projects versioned,
     * with configuration specified by the [shared] block.
     *
     * @param projects Project references
     */
    fun create(vararg projects: ProjectDescriptor) = projects.forEach(::create)

    /**
     * Includes the provided projects and makes them versioned,
     * with configuration specified by the [shared] block.
     *
     * @param projects Project paths
     */
    fun create(vararg projects: String) = projects.forEach(::create)

    /**
     * Makes the specified projects versioned,
     * with configuration specified by the [shared] block.
     *
     * @param projects Project references
     */
    fun create(projects: Iterable<ProjectDescriptor>) = projects.forEach(::create)

    /**
     * Includes the provided projects and makes them versioned,
     * with configuration specified by the [shared] block.
     *
     * @param projects Project paths
     * @return 🍌 to prevent JVM signature crash. Do whatever you want with it
     */
    fun create(projects: Iterable<String>) = projects.forEach(::create).let { "🍌" }

    /**
     * Makes the specified projects versioned
     * and assigns the provided configuration to it.
     *
     * @param projects Project references
     * @param builder Configuration scope
     */
    fun create(vararg projects: ProjectDescriptor, builder: Action<StonecutterSetupBuilder>) =
        projects.forEach { create(it, StonecutterSetupBuilder(builder)) }

    /**
     * Makes the specified projects versioned
     * and assigns the provided configuration to it.
     *
     * @param projects Project paths
     * @param builder Configuration scope
     */
    fun create(vararg projects: String, builder: Action<StonecutterSetupBuilder>) =
        projects.forEach { create(it, StonecutterSetupBuilder(builder)) }

    /**
     * Makes the specified projects versioned
     * and assigns the provided configuration to it.
     *
     * @param projects Project references
     * @param builder Configuration scope
     */
    fun create(projects: Iterable<ProjectDescriptor>, builder: Action<StonecutterSetupBuilder>) =
        projects.forEach { create(it, StonecutterSetupBuilder(builder)) }

    /**
     * Makes the specified projects versioned
     * and assigns the provided configuration to it.
     *
     * @param projects Project paths
     * @param builder Configuration scope
     * @return 🍌 to prevent JVM signature crash. Do whatever you want with it
     */
    fun create(projects: Iterable<String>, builder: Action<StonecutterSetupBuilder>) =
        projects.forEach { create(it, StonecutterSetupBuilder(builder)) }.let { "🍌" }
}