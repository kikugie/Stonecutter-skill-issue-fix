package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.ParameterHolder
import dev.kikugie.stonecutter.validateId
import org.gradle.api.Project
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.nio.file.Path

/**
 * Provides functions to configure Stonecutter.
 * Shared by [StonecutterBuild] and [ParameterHolder]
 */
@Suppress("unused")
abstract class BuildConfiguration(private val project: Project) {
    internal val data: BuildParameters = BuildParameters()

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given identifier and replacement value.
     * Meant to be used with Kotlin DSL.
     * ```kotlin
     * swaps["..."] = "..."
     * ```
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    val swaps = object : MapSetter<Identifier, String> {
        override fun set(key: Identifier, value: String) = swap(key, value)
    }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given identifier and corresponding boolean value.
     * Meant to be used with Kotlin DSL.
     * ```kotlin
     * consts["..."] = true
     * ```
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    val consts = object : MapSetter<Identifier, Boolean> {
        override fun set(key: Identifier, value: Boolean) = const(key, value)
    }

    // link: wiki-build-deps
    /**
     * Adds a dependency to the semver checks with the given identifier and corresponding version.
     * Meant to be used with Kotlin DSL.
     * ```kotlin
     * dependencies["..."] = "..."
     * ```
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    val dependencies = object : MapSetter<Identifier, SemanticVersion> {
        override fun set(key: Identifier, value: SemanticVersion) = dependency(key, value)
    }

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    fun swap(identifier: Identifier, replacement: String) {
        data.swaps[identifier.validateId()] = replacement
    }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun const(identifier: Identifier, value: Boolean) {
        data.constants[identifier.validateId()] = value
    }

    // link: wiki-build-deps
    /**
     * Adds a dependency to the predicate checks with the given [identifier] and corresponding [version].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    fun dependency(identifier: Identifier, version: SemanticVersion) {
        data.dependencies[identifier.validateId()] = version.validateVersion()
    }

    /**
     * Excludes a file or directory from being processed.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("To be reworked in 0.6 with inverted behaviour using `include()`")
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    fun exclude(path: File) {
        exclude(path.toPath())
    }

    /**
     * Excludes a file or directory from being processed.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("To be reworked in 0.6 with inverted behaviour using `include()`")
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    fun exclude(path: Path) {
        data.excludedPaths.add(path)
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Path to the file relative to the parent project directory (where `stonecutter.gradle[.kts]` is located)
     * or a file extension qualifier (i.e. `*.json`).
     */
    @Deprecated("To be reworked in 0.6 with inverted behaviour using `include()`")
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    fun exclude(path: String) {
        require(path.isNotBlank()) { "Path must not be empty" }
        if (path.startsWith("*.")) data.excludedExtensions.add(path.substring(2))
        else data.excludedPaths.add(project.file(path).toPath())
    }

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    fun swap(identifier: Identifier, replacement: () -> String) {
        swap(identifier, replacement())
    }

    // link: wiki-build-swaps
    /**
     * Adds swaps with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    fun swaps(vararg values: Pair<Identifier, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    fun swaps(values: Iterable<Pair<Identifier, String>>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun const(identifier: Identifier, value: () -> Boolean) {
        const(identifier, value())
    }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun consts(vararg values: Pair<Identifier, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun consts(values: Iterable<Pair<Identifier, Boolean>>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Adds multiple constants from the [choices], checking whenever each is equal to the given [value]
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun consts(value: Identifier, vararg choices: Identifier) {
        choices.forEach { const(it, it == value) }
    }

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value]
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    fun consts(value: Identifier, choices: Iterable<Identifier>) {
        choices.forEach { const(it, it == value) }
    }

    // link: wiki-build-deps
    /**
     * Adds a dependency to the predicate checks with the given [identifier] and corresponding [version].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    fun dependency(identifier: Identifier, version: () -> SemanticVersion) {
        dependency(identifier, version())
    }

    // link: wiki-build-deps
    /**
     * Adds dependencies to the predicate checks from provided identifier - version pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    fun dependencies(vararg values: Pair<Identifier, SemanticVersion>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    // link: wiki-build-deps
    /**
     * Adds dependencies to the predicate checks from provided identifier - version pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    fun dependencies(values: Iterable<Pair<Identifier, SemanticVersion>>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    internal fun from(other: BuildConfiguration): Unit = with(data) {
        swaps.putAll(other.data.swaps)
        constants.putAll(other.data.constants)
        dependencies.putAll(other.data.dependencies)
        excludedPaths.addAll(other.data.excludedPaths)
        excludedExtensions.addAll(other.data.excludedExtensions)
    }
}