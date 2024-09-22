package dev.kikugie.stonecutter.build

import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.lexer.TokenMatcher.Companion.isValidIdentifier
import dev.kikugie.stonecutter.MapSetter
import dev.kikugie.stonecutter.controller.StonecutterController
import org.gradle.api.Project
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
    val swaps = object : MapSetter<String, String> {
        override fun set(key: String, value: String) = swap(key, value)
    }

    /**
     * Creates a constant with the given value. Meant to be used with Kotlin DSL:
     * ```kotlin
     * consts["..."] = true
     * ```
     */
    val consts = object : MapSetter<String, Boolean> {
        override fun set(key: String, value: Boolean) = const(key, value)
    }

    /**
     * Creates a dependency with the given value. Meant to be used with Kotlin DSL:
     * ```kotlin
     * dependencies["..."] = "..."
     * ```
     */
    val dependencies = object : MapSetter<String, String> {
        override fun set(key: String, value: String) = dependency(key, value)
    }

    /**
     * Creates a swap id.
     *
     * @param identifier Swap name
     * @param replacement Replacement string
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: String, replacement: String) {
        data.swaps[validateId(identifier)] = replacement
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier Constant name
     * @param value Boolean value
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: String, value: Boolean) {
        data.constants[validateId(identifier)] = value
    }

    /**
     * Adds a dependency to the semver checks.
     *
     * @param identifier Dependency name
     * @param version Dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: String, version: String) {
        data.dependencies[validateId(identifier)] = validateSemver(version)
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Absolute path to the file.
     */
    fun exclude(path: File) {
        exclude(path.toPath())
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Absolute path to the file.
     */
    fun exclude(path: Path) {
        data.excludedPaths.add(path)
    }

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Path to the file relative to the parent project directory (where `stonecutter.gradle[.kts]` is located)
     * or a file extension qualifier (i.e. `*.json`).
     */
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
    fun swap(identifier: String, replacement: () -> String) {
        swap(identifier, replacement())
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values Entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(vararg values: Pair<String, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    /**
     * Adds provided id to value pairs to the swap map.
     *
     * @param values Entries of ids to replacements
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swaps(values: Iterable<Pair<String, String>>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    /**
     * Creates a constant accessible in stonecutter conditions.
     *
     * @param identifier Constant name
     * @param value Boolean value
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun const(identifier: String, value: () -> Boolean) {
        const(identifier, value())
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values Entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(vararg values: Pair<String, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Adds provided id to value pairs to the constant map.
     *
     * @param values Entries of ids to boolean values
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(values: Iterable<Pair<String, Boolean>>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    /**
     * Adds multiple constants to the constant map.
     *
     * @param value The value to compare against.
     * @param choices The list of choices to create constants for.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(value: String, vararg choices: String) {
        choices.forEach { const(it, it == value) }
    }

    /**
     * Adds multiple constants to the constant map.
     *
     * @param value The value to compare against.
     * @param choices The list of choices to create constants for.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#constants">Wiki</a>
     */
    fun consts(value: String, choices: Iterable<String>) {
        choices.forEach { const(it, it == value) }
    }

    /**
     * Adds a dependency to the semver checks.
     *
     * @param identifier Dependency name
     * @param version Dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: String, version: () -> String) {
        dependency(identifier, version())
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values Entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependencies(vararg values: Pair<String, String>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    /**
     * Adds provided id to value pairs to the semver checks.
     *
     * @param values Entries of ids to versions
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependencies(values: Iterable<Pair<String, String>>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    internal fun from(other: BuildConfiguration): Unit = with(data) {
        swaps.putAll(other.data.swaps)
        constants.putAll(other.data.constants)
        dependencies.putAll(other.data.dependencies)
        excludedPaths.addAll(other.data.excludedPaths)
        excludedExtensions.addAll(other.data.excludedExtensions)
    }

    private fun validateId(id: String) = id.apply {
        require(all { it.isValidIdentifier() }) { "Invalid identifier: $this" }
    }

    private fun validateSemver(version: String) = try {
        VersionParser.parse(version, full = true).value
    } catch (e: VersionParsingException) {
        throw IllegalArgumentException("Invalid semantic version: $version").apply {
            initCause(e)
        }
    }
}