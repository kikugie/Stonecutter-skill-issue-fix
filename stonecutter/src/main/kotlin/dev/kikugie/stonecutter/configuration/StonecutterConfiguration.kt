package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterController
import java.io.File
import java.nio.file.Path

/**
 * Provides functions to configure Stonecutter.
 * Shared by [StonecutterBuild] and [StonecutterController]
 */
@Suppress("unused")
interface StonecutterConfiguration {
    /**
     * Creates a swap id.
     *
     * @param identifier Swap name
     * @param replacement Replacement string
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#swaps">Wiki</a>
     */
    fun swap(identifier: String, replacement: String)

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
    fun const(identifier: String, value: Boolean)

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
    fun dependency(identifier: String, version: String)

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
    fun exclude(path: Path)

    /**
     * Excludes a file or directory from being processed.
     *
     * @param path Path to the file relative to the parent project directory (where `stonecutter.gradle[.kts]` is located)
     * or a file extension qualifier (i.e. `*.json`).
     */
    fun exclude(path: String)

    /**
     * Enables Stonecutter debugging utilities.
     *
     * Currently, it creates human-readable ASTs in the cache folder
     * and adds stack traces to reported exceptions.
     */
    var debug: Boolean

    /**
     * Enables Stonecutter debugging utilities.
     *
     * Currently, it creates human-readable ASTs in the cache folder
     * and adds stack traces to reported exceptions.
     *
     * @param state Whenever the debug mode is enabled. Defaults to `false`
     */
    fun debug(state: Boolean) {
        debug = state
    }
}