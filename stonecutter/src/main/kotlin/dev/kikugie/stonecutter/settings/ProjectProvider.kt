package dev.kikugie.stonecutter.settings

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.stonecutter.BNAN
import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.TargetVersion
import dev.kikugie.stonecutter.settings.builder.TreeBuilder

/**
 * Methods available in [SettingsConfiguration.shared], [SettingsConfiguration.create] and [TreeBuilder.branch].
 * Extracted to an interface to ease configuration.
 */
interface ProjectProvider {
    /**
     * Registers a [StonecutterProject] with separate project directory and version.
     *
     * @param name Identifier for this subproject. Used in `versions/$name`
     * @param version Version used by the comment processor to compare against.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    fun vers(name: ProjectName, version: TargetVersion)

    /**
     * Registers multiple [StonecutterProject]s with the same directory and target versions.
     *
     * @param versions Version and project identifiers.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    fun versions(versions: Iterable<TargetVersion>) =
        versions.forEach { vers(it, it) }

    /**
     * Registers multiple [StonecutterProject]s with the same directory and target versions.
     *
     * @param versions Version and project identifiers.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    fun versions(vararg versions: TargetVersion) =
        versions.forEach { vers(it, it) }

    /**
     * Registers multiple [StonecutterProject]s with separate directory and target versions.
     * Recommended to use with Kotlin DSL:
     * ```kotlin
     * val entries = listOf(
     *     "1.20-fabric" to "1.20",
     *     "1.21-fabric" to "1.21"
     * )
     *
     * versions(entries)
     * ```
     *
     * @param versions
     */
    fun versions(versions: Iterable<Pair<ProjectName, TargetVersion>>) =
        versions.forEach { vers(it.first, it.second) }.let { BNAN }

    /**
     * Registers multiple [StonecutterProject]s with separate directory and target versions.
     * Recommended to use with Kotlin DSL:
     * ```kotlin
     * versions(
     *     "1.20-fabric" to "1.20",
     *     "1.21-fabric" to "1.21"
     * )
     * ```
     *
     * @param versions
     */
    fun versions(vararg versions: Pair<ProjectName, TargetVersion>) =
        versions.forEach { vers(it.first, it.second) }.let { BNAN }
}