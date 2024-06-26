package dev.kikugie.stonecutter

import dev.kikugie.semver.SemanticVersionParser
import java.io.File
import java.nio.file.Path

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
     * @param replacement Replacement string provider
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
     * @param value Boolean value provider
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
     * Adds a dependency to the semver checks.
     *
     * @param identifier Dependency name
     * @param version Dependency version to check against in semantic version format
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#dependencies">Wiki</a>
     */
    fun dependency(identifier: String, version: String)

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

    /**
     * Parses both parameters as semantic versions and compares them.
     *
     * @param left Version on the left side of the comparison
     * @param right Version on the right side of the comparison
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    fun compare(left: String, right: String) =
        SemanticVersionParser.parse(left).compareTo(SemanticVersionParser.parse(right))

    /**
     * Parses both parameters as semantic versions and compares them.
     *
     * This function is available inside a `stonecutter` block when using Kotlin DSL.
     *
     * @receiver Version on the left side of the comparison
     * @param other Version on the right side of the comparison
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    infix fun String.comp(other: String) = compare(this, other)
}