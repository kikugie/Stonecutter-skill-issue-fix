package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.SemanticVersion
import dev.kikugie.stonecutter.StonecutterAPI
import dev.kikugie.stonecutter.data.parameters.BuildParameters

/**Delegates set operation. Meant to be used with Kotlin DSL.*/
interface MapSetter<K, V> {
    /**Sets the [value] for the specified [key] in the underlying map.*/
    operator fun set(key: K, value: V)
}

/**Declutters swap function variants, directing them to a single implementation.*/
interface SwapVariants {
    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     *
     * @sample stonecutter_samples.swaps.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swap(identifier: Identifier, replacement: String)

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     *
     * @sample stonecutter_samples.swaps.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swap(identifier: Identifier, replacement: () -> String) =
        swap(identifier, replacement())

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swaps(vararg values: Pair<Identifier, String>) =
        swaps(values.asIterable())

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swaps(values: Iterable<Pair<Identifier, String>>) =
        values.forEach { (id, str) -> swap(id, str) }

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI fun swaps(values: Map<Identifier, String>) =
        swaps(values.toList())

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given identifier and replacement value.
     *
     * @sample stonecutter_samples.swaps.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI val swaps get() = object : MapSetter<Identifier, String> {
        override fun set(key: Identifier, value: String) = swap(key, value)
    }
}

/**Declutters const function variants, directing them to a single implementation.*/
interface ConstantVariants {
    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     *
     * @sample stonecutter_samples.constants.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun const(identifier: Identifier, value: Boolean)

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     *
     * @sample stonecutter_samples.constants.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun const(identifier: Identifier, value: () -> Boolean) =
        const(identifier, value())

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(vararg values: Pair<Identifier, Boolean>) =
        consts(values.asIterable())

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(values: Iterable<Pair<Identifier, Boolean>>) =
        values.forEach { (id, str) -> const(id, str) }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(values: Map<Identifier, Boolean>) =
        consts(values.toList())

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value].
     *
     * @sample stonecutter_samples.constants.choices_vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(value: Identifier, vararg choices: Identifier) =
        consts(value, choices.asIterable())

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value].
     *
     * @sample stonecutter_samples.constants.choices_iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI fun consts(value: Identifier, choices: Iterable<Identifier>) =
        choices.forEach { const(it, it == value) }

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given identifier and corresponding boolean value.
     *
     * @sample stonecutter_samples.constants.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI val consts get() = object : MapSetter<Identifier, Boolean> {
        override fun set(key: Identifier, value: Boolean) = const(key, value)
    }
}

/**Declutters dependency function variants, directing them to a single implementation.*/
interface DependencyVariants {
    // link: wiki-build-deps
    /**
     * Creates a dependency to the predicate checks with the given [identifier] and corresponding [version].
     *
     * @sample stonecutter_samples.dependencies.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependency(identifier: Identifier, version: SemanticVersion)

    // link: wiki-build-deps
    /**
     * Creates a dependency to the predicate checks with the given [identifier] and corresponding [version].
     *
     * @sample stonecutter_samples.dependencies.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependency(identifier: Identifier, version: () -> SemanticVersion) =
        dependency(identifier, version())

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependencies(vararg values: Pair<Identifier, SemanticVersion>) =
        dependencies(values.asIterable())

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependencies(values: Iterable<Pair<Identifier, SemanticVersion>>) =
        values.forEach { (id, ver) -> dependency(id, ver) }

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI fun dependencies(values: Map<Identifier, SemanticVersion>) =
        dependencies(values.toList())

    // link: wiki-build-deps
    /**
     * Creates a dependency to the semver checks with the given identifier and corresponding version.
     *
     * @sample stonecutter_samples.dependencies.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI val dependencies get() = object : MapSetter<Identifier, SemanticVersion> {
        override fun set(key: Identifier, value: SemanticVersion) = dependency(key, value)
    }
}

/**Declutters file filtering function variants, directing them to a single implementation.*/
interface FilterVariants {
    /**
     * Allows provided [extensions] to be processed by Stonecutter.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.allowExtensions.vararg
     * @see BuildParameters.extensions
     */
    @StonecutterAPI fun allowExtensions(vararg extensions: String) =
        allowExtensions(extensions.asIterable())

    /**
     * Allows provided [extensions] to be processed by Stonecutter.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.allowExtensions.iterable
     * @see BuildParameters.extensions
     */
    @StonecutterAPI fun allowExtensions(extensions: Iterable<String>)

    /**
     * Replaces allowed extensions with the provided list.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.overrideExtensions.vararg
     */
    @StonecutterAPI fun overrideExtensions(vararg extensions: String) =
        overrideExtensions(extensions.asIterable())

    /**
     * Replaces allowed extensions with the provided list.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.overrideExtensions.iterable
     */
    @StonecutterAPI fun overrideExtensions(extensions: Iterable<String>)

    /**
     * Excludes specific files or directories from being processed.
     * **Paths must be relative to the branch's directory and start with `src/`**.
     *
     * @sample stonecutter_samples.excludeFiles.vararg
     */
    @StonecutterAPI fun excludeFiles(vararg files: String) =
        excludeFiles(files.asIterable())

    /**
     * Excludes specific files or directories from being processed.
     * **Paths must be relative to the branch's directory and start with `src/`**.
     *
     * @sample stonecutter_samples.excludeFiles.iterable
     */
    @StonecutterAPI fun excludeFiles(files: Iterable<String>)
}