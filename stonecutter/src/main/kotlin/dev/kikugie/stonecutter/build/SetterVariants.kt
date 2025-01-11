package dev.kikugie.stonecutter.build

import dev.kikugie.semver.Version
import dev.kikugie.semver.SemanticVersion as SemanticVersionImpl
import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.ReplacementPhase.LAST
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import org.intellij.lang.annotations.Language

internal class CheckedMap<K, V>(
    private val delegate: MutableMap<K, V>,
    private val check: (K, V) -> Unit
) : MutableMap<K, V> by delegate {
    override fun put(key: K, value: V): V? = check(key, value) then delegate.put(key, value)
    override fun putAll(from: Map<out K, V>) = delegate.forEach { (k, v) -> check(k, v) } then delegate.putAll(from)
}

/**Declutters swap public function variants, directing them to a single implementation.*/
public interface SwapVariants {
    // link: wiki-build-swaps
    /**
     * Creates a swap with the given identifier and replacement value.
     *
     * @sample stonecutter_samples.swaps.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public val swaps: MutableMap<Identifier, String>

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     *
     * @sample stonecutter_samples.swaps.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public fun swap(identifier: Identifier, replacement: String): Unit =
        swaps.set(identifier, replacement)

    // link: wiki-build-swaps
    /**
     * Creates a swap with the given [identifier] and [replacement] value.
     *
     * @sample stonecutter_samples.swaps.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public fun swap(identifier: Identifier, replacement: () -> String): Unit =
        swap(identifier, replacement())

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public fun swaps(vararg values: Pair<Identifier, String>): Unit =
        swaps(values.asIterable())

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public fun swaps(values: Iterable<Pair<Identifier, String>>): Unit =
        values.forEach { (id, str) -> swap(id, str) }

    // link: wiki-build-swaps
    /**
     * Creates swaps with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.swaps.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#value-swaps">Wiki page</a>
     */
    @StonecutterAPI public fun swaps(values: Map<Identifier, String>): Unit =
        swaps(values.toList())
}

/**Declutters const public function variants, directing them to a single implementation.*/
public interface ConstantVariants {
    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given identifier and corresponding boolean value.
     *
     * @sample stonecutter_samples.constants.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public val consts: MutableMap<Identifier, Boolean>

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     *
     * @sample stonecutter_samples.constants.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun const(identifier: Identifier, value: Boolean): Unit =
        consts.set(identifier, value)

    // link: wiki-build-consts
    /**
     * Creates a constant accessible in stonecutter conditions with the given [identifier] and corresponding boolean [value].
     *
     * @sample stonecutter_samples.constants.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun const(identifier: Identifier, value: () -> Boolean): Unit =
        const(identifier, value())

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun consts(vararg values: Pair<Identifier, Boolean>): Unit =
        consts(values.asIterable())

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun consts(values: Iterable<Pair<Identifier, Boolean>>): Unit =
        values.forEach { (id, str) -> const(id, str) }

    // link: wiki-build-consts
    /**
     * Creates constants with provided identifier - value pairs.
     *
     * @sample stonecutter_samples.constants.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun consts(values: Map<Identifier, Boolean>): Unit =
        consts(values.toList())

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value].
     *
     * @sample stonecutter_samples.constants.choices_vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun consts(value: Identifier, vararg choices: Identifier): Unit =
        consts(value, choices.asIterable())

    // link: wiki-build-consts
    /**
     * Creates multiple constants from the [choices], checking whenever each is equal to the given [value].
     *
     * @sample stonecutter_samples.constants.choices_iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants">Wiki page</a>
     */
    @StonecutterAPI public fun consts(value: Identifier, choices: Iterable<Identifier>): Unit =
        choices.forEach { const(it, it == value) }
}

/**Declutters dependency public function variants, directing them to a single implementation.*/
public interface DependencyVariants {
    /**Mutable [Version] map that allows setting values from strings.*/
    public class VersionMap(delegate: MutableMap<Identifier, Version>) : MutableMap<Identifier, Version> by delegate {
        /**Converts the provided [value] to [SemanticVersionImpl] and stores it in the backing map.*/
        public operator fun set(key: Identifier, value: SemanticVersion): Unit = set(key, value.validateVersion())

        /**Converts the provided [value] to [SemanticVersionImpl] and stores it in the backing map.*/
        public fun put(key: Identifier, value: Identifier): Version? = put(key, value.validateVersion())

        /**Converts values in the provided [map] to [SemanticVersionImpl] and stores each pair in the backing map.*/
        public fun putAll(map: Map<out Identifier, Any>): Unit = map.forEach { (k, v) -> set(k, convert(v)) }

        private fun convert(value: Any): Version = when (value) {
            is String -> value.validateVersion()
            is Version -> value
            else -> throw IllegalArgumentException("Invalid version type: ${value::class.simpleName}")
        }
    }
    // link: wiki-build-deps
    /**
     * Creates a dependency to the semver checks with the given identifier and corresponding version.
     *
     * @sample stonecutter_samples.dependencies.setter
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public val dependencies: VersionMap

    // link: wiki-build-deps
    /**
     * Creates a dependency to the predicate checks with the given [identifier] and corresponding [version].
     *
     * @sample stonecutter_samples.dependencies.single
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public fun dependency(identifier: Identifier, version: SemanticVersion): Unit =
        dependencies.set(identifier, version)

    // link: wiki-build-deps
    /**
     * Creates a dependency to the predicate checks with the given [identifier] and corresponding [version].
     *
     * @sample stonecutter_samples.dependencies.provider
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public fun dependency(identifier: Identifier, version: () -> SemanticVersion): Unit =
        dependency(identifier, version())

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.vararg
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public fun dependencies(vararg values: Pair<Identifier, SemanticVersion>): Unit =
        dependencies(values.asIterable())

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.iterable
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public fun dependencies(values: Iterable<Pair<Identifier, SemanticVersion>>): Unit =
        values.forEach { (id, ver) -> dependency(id, ver) }

    // link: wiki-build-deps
    /**
     * Creates dependencies to the predicate checks from provided identifier - version pairs.
     *
     * @sample stonecutter_samples.dependencies.map
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-dependencies">Wiki page</a>
     */
    @StonecutterAPI public fun dependencies(values: Map<Identifier, SemanticVersion>): Unit =
        dependencies(values.toList())
}

public interface ReplacementVariants {
    /**
     * Creates a plain string find&replace entry for the file processor.
     * When [direction] is `true` it will replace [source] with [target],
     * or vice versa when [direction] is `false`.
     *
     * @sample stonecutter_samples.replacements.basic_correct
     * @sample stonecutter_samples.replacements.basic_ambiguous
     * @sample stonecutter_samples.replacements.basic_circular
     * @throws IllegalArgumentException If [source] already has a registered replacement
     * or if [source] and [target] create a circular reference with registered entries.
     */
    @StonecutterAPI public fun replacement(
        phase: ReplacementPhase,
        direction: Boolean,
        source: String,
        target: String
    )

    /**
     * Creates a plain string find&replace entry for the file processor.
     * When [direction] is `true` it will replace [source] with [target],
     * or vice versa when [direction] is `false`.
     *
     * @sample stonecutter_samples.replacements.basic_correct
     * @sample stonecutter_samples.replacements.basic_ambiguous
     * @sample stonecutter_samples.replacements.basic_circular
     * @throws IllegalArgumentException If [source] already has a registered replacement
     * or if [source] and [target] create a circular reference with registered entries.
     */
    @StonecutterAPI public fun replacement(
        direction: Boolean,
        source: String,
        target: String
    ): Unit = replacement(LAST, direction, source, target)

    /**
     * Creates a regex replacement entry for the file processor.
     * When [direction] is `true` it will find all occurrences of the [sourcePattern]
     * and replace them with [targetValue].
     * Otherwise, it will do the same for [targetPattern] and [sourceValue].
     *
     * **This public functionality doesn't have the same safety features as plain-string replacements**, namely:
     * - It doesn't group entries or check for ambiguous replacements, therefore, **replacements are order-dependent**.
     * - It doesn't verify whenever the replaced value can be reversed when switching back to the original version.
     *   **When using this, it's your responsibility to make sure replacements are reversible.**
     *
     * Provided expressions are evaluated after plain-text ones.
     */
    @StonecutterDelicate public fun replacement(
        phase: ReplacementPhase = LAST,
        direction: Boolean,
        @Language("regex") sourcePattern: String,
        targetValue: String,
        @Language("regex") targetPattern: String,
        sourceValue: String
    )

    /**
     * Creates a regex replacement entry for the file processor.
     * When [direction] is `true` it will find all occurrences of the [sourcePattern]
     * and replace them with [targetValue].
     * Otherwise, it will do the same for [targetPattern] and [sourceValue].
     *
     * **This public functionality doesn't have the same safety features as plain-string replacements**, namely:
     * - It doesn't group entries or check for ambiguous replacements, therefore, **replacements are order-dependent**.
     * - It doesn't verify whenever the replaced value can be reversed when switching back to the original version.
     *   **When using this, it's your responsibility to make sure replacements are reversible.**
     *
     * Provided expressions are evaluated after plain-text ones.
     */
    @StonecutterDelicate public fun replacement(
        direction: Boolean,
        @Language("regex") sourcePattern: String,
        targetValue: String,
        @Language("regex") targetPattern: String,
        sourceValue: String
    ): Unit = replacement(LAST, direction, sourcePattern, targetValue, targetPattern, sourceValue)
}

/**Declutters file filtering public function variants, directing them to a single implementation.*/
public interface FilterVariants {
    /**
     * Allows provided [extensions] to be processed by Stonecutter.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.allowExtensions.vararg
     * @see BuildParameters.extensions
     */
    @StonecutterAPI public fun allowExtensions(vararg extensions: String): Unit =
        allowExtensions(extensions.asIterable())

    /**
     * Allows provided [extensions] to be processed by Stonecutter.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.allowExtensions.iterable
     * @see BuildParameters.extensions
     */
    @StonecutterAPI public fun allowExtensions(extensions: Iterable<String>)

    /**
     * Replaces allowed extensions with the provided list.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.overrideExtensions.vararg
     */
    @StonecutterAPI public fun overrideExtensions(vararg extensions: String): Unit =
        overrideExtensions(extensions.asIterable())

    /**
     * Replaces allowed extensions with the provided list.
     * **Entries must not start with a dot**.
     *
     * @sample stonecutter_samples.overrideExtensions.iterable
     */
    @StonecutterAPI public fun overrideExtensions(extensions: Iterable<String>)

    /**
     * Excludes specific files or directories from being processed.
     * **Paths must be relative to the branch's directory and start with `src/`**.
     *
     * @sample stonecutter_samples.excludeFiles.vararg
     */
    @StonecutterAPI public fun excludeFiles(vararg files: String): Unit =
        excludeFiles(files.asIterable())

    /**
     * Excludes specific files or directories from being processed.
     * **Paths must be relative to the branch's directory and start with `src/`**.
     *
     * @sample stonecutter_samples.excludeFiles.iterable
     */
    @StonecutterAPI public fun excludeFiles(files: Iterable<String>)
}