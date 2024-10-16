package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.validateId
import org.gradle.api.Project
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.nio.file.Path

/**
 * Provides functions to configure Stonecutter.
 * Shared by [StonecutterBuild] and [StonecutterController]
 */
@Suppress("unused")
abstract class BuildConfiguration(private val project: Project) {
    internal val data: BuildParameters = BuildParameters()

    /**
     * Creates a swap with the given value. Meant to be used with Kotlin DSL:
     * ```kotlin
     * swaps["..."] = "..."
     * ```
     */
    val swaps = object : MapSetter<Identifier, String> {
        override fun set(key: Identifier, value: String) = swap(key, value)
    }

    /**
     * Creates a constant with the given value. Meant to be used with Kotlin DSL:
     * ```kotlin
     * consts["..."] = true
     * ```
     */
    val consts = object : MapSetter<Identifier, Boolean> {
        override fun set(key: Identifier, value: Boolean) = const(key, value)
    }

    /**
     * Creates a dependency with the given value. Meant to be used with Kotlin DSL:
     * ```kotlin
     * dependencies["..."] = "..."
     * ```
     */
    val dependencies = object : MapSetter<Identifier, SemanticVersion> {
        override fun set(key: Identifier, value: SemanticVersion) = dependency(key, value)
    }

    /**
     * Creates a swap id.
     *
     * @param identifier Swap name
     * @param replacement Replacement string
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: Identifier, replacement: String) {
        data.swaps[identifier.validateId()] = replacement
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier Constant name
     * @param value Boolean value
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: Identifier, value: Boolean) {
        data.constants[identifier.validateId()] = value
    }

    /**
     * Adds a dependency to the semver checks.
     *
     * @param identifier Dependency name
     * @param version Dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: Identifier, version: SemanticVersion) {
        data.dependencies[identifier.validateId()] = version.validateVersion()
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Absolute path to the file.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("To be reworked in 0.6 with inverted behaviour using `include()`")
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    fun exclude(path: File) {
        exclude(path.toPath())
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Absolute path to the file.
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

    /**
     * Creates a swap id.
     *
     * @param identifier Swap name
     * @param replacement Replacement string
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: Identifier, replacement: () -> String) {
        swap(identifier, replacement())
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values Entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(vararg values: Pair<Identifier, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values Entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(values: Iterable<Pair<Identifier, String>>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier Constant name
     * @param value Boolean value
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: Identifier, value: () -> Boolean) {
        const(identifier, value())
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values Entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(vararg values: Pair<Identifier, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values Entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(values: Iterable<Pair<Identifier, Boolean>>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Adds multiple constants to the constant map.
     *
     * @param value The value to compare against.
     * @param choices The list of choices to create constants for.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(value: Identifier, vararg choices: Identifier) {
        choices.forEach { const(it, it == value) }
    }

    /**
     * Adds multiple constants to the constant map.
     *
     * @param value The value to compare against.
     * @param choices The list of choices to create constants for.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(value: Identifier, choices: Iterable<Identifier>) {
        choices.forEach { const(it, it == value) }
    }

    /**
     * Adds a dependency to the semver checks.
     *
     * @param identifier Dependency name
     * @param version Dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: Identifier, version: () -> SemanticVersion) {
        dependency(identifier, version())
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values Entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependencies(vararg values: Pair<Identifier, SemanticVersion>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values Entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
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