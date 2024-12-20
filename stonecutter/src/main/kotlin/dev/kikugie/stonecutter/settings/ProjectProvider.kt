package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.tree.TreeBuilder

/**
 * Methods available in [SettingsConfiguration.shared], [SettingsConfiguration.create] and [TreeBuilder.branch].
 * Extracted to an interface to ease configuration.
 */
interface ProjectProvider {
    /**
     * Registers a [StonecutterProject] with separate project directory and version.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    @StonecutterAPI fun vers(name: Identifier, version: AnyVersion)

    /**
     * Registers multiple [StonecutterProject]s with the same directory and target versions.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    @StonecutterAPI fun versions(versions: Iterable<AnyVersion>) =
        versions.forEach { vers(it, it) }

    /**
     * Registers multiple [StonecutterProject]s with the same directory and target versions.
     * Recommended to use a [SemanticVersion], but plain string values are accepted too.
     */
    @StonecutterAPI fun versions(vararg versions: AnyVersion) =
        versions.forEach { vers(it, it) }

    /**
     * Registers multiple [StonecutterProject]s with separate directory and target versions.
     * Recommended to use with Kotlin DSL.
     * ```kotlin
     * val entries = listOf(
     *     "1.20-fabric" to "1.20",
     *     "1.21-fabric" to "1.21"
     * )
     *
     * versions(entries)
     * ```
     */
    @StonecutterAPI fun versions(versions: Iterable<Pair<Identifier, AnyVersion>>) =
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
     */
    @StonecutterAPI fun versions(vararg versions: Pair<Identifier, AnyVersion>) =
        versions.forEach { vers(it.first, it.second) }.let { BNAN }
}