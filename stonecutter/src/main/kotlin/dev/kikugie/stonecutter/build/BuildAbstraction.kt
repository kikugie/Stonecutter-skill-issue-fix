package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.container.ConfigurationService.Companion.of
import dev.kikugie.stonecutter.data.parameters.BuildParameters

abstract class BuildAbstraction(protected val hierarchy: ProjectHierarchy) {
    /**
     * Delegates set operation. Meant to be used with Kotlin DSL.
     * @param K Key type
     * @param V Value type
     */
    interface MapSetter<K, V> {
        /**Sets the [value] for the specified [key] in the underlying map.*/
        operator fun set(key: K, value: V)
    }

    protected val data: BuildParameters = checkNotNull(StonecutterPlugin.SERVICE.of(hierarchy).build) {
        "Stonecutter build parameters not found"
    }


    // link: wiki-build-swaps
    /**
     * Creates a swap with the given identifier and replacement value.
     * Meant to be used with Kotlin DSL.
     * ```kotlin
     * swaps["..."] = "..."
     * ```
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI val swaps = object : MapSetter<Identifier, String> {
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
    @StonecutterAPI val consts = object : MapSetter<Identifier, Boolean> {
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
    @StonecutterAPI val dependencies = object : MapSetter<Identifier, SemanticVersion> {
        override fun set(key: Identifier, value: SemanticVersion) = dependency(key, value)
    }

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swap(identifier: Identifier, replacement: String) {
        data.swaps[identifier.validateId()] = replacement
    }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun const(identifier: Identifier, value: Boolean) {
        data.constants[identifier.validateId()] = value
    }

    // link: wiki-build-deps
    /**
     * Adds a dependency to the predicate checks with the given [identifier] and corresponding [version].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependency(identifier: Identifier, version: SemanticVersion) {
        data.dependencies[identifier.validateId()] = version.validateVersion()
    }

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swap(identifier: Identifier, replacement: () -> String) {
        swap(identifier, replacement())
    }

    // link: wiki-build-swaps
    /**
     * Adds swaps with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swaps(vararg values: Pair<Identifier, String>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swaps(values: Iterable<Pair<Identifier, String>>) {
        values.forEach { (id, str) -> swap(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun const(identifier: Identifier, value: () -> Boolean) {
        const(identifier, value())
    }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(vararg values: Pair<Identifier, Boolean>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(values: Iterable<Pair<Identifier, Boolean>>) {
        values.forEach { (id, str) -> const(id, str) }
    }

    // link: wiki-build-consts
    /**
     * Adds multiple constants from the [choices], checking whenever each is equal to the given [value]
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(value: Identifier, vararg choices: Identifier) {
        choices.forEach { const(it, it == value) }
    }

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value]
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(value: Identifier, choices: Iterable<Identifier>) {
        choices.forEach { const(it, it == value) }
    }

    // link: wiki-build-deps
    /**
     * Adds a dependency to the predicate checks with the given [identifier] and corresponding [version].
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependency(identifier: Identifier, version: () -> SemanticVersion) {
        dependency(identifier, version())
    }

    // link: wiki-build-deps
    /**
     * Adds dependencies to the predicate checks from provided identifier - version pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependencies(vararg values: Pair<Identifier, SemanticVersion>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    // link: wiki-build-deps
    /**
     * Adds dependencies to the predicate checks from provided identifier - version pairs.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependencies(values: Iterable<Pair<Identifier, SemanticVersion>>) {
        values.forEach { (id, ver) -> dependency(id, ver) }
    }

    internal fun from(other: BuildAbstraction): Unit = with(data) {
        swaps.putAll(other.data.swaps)
        constants.putAll(other.data.constants)
        dependencies.putAll(other.data.dependencies)
        excludedPaths.addAll(other.data.excludedPaths)
        excludedExtensions.addAll(other.data.excludedExtensions)
    }
}