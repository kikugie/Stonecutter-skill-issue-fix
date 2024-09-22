package dev.kikugie.stonecutter

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.semver.StringVersion
import dev.kikugie.semver.VersionParser
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract

/**
 * Provides a set of pure functions to ease Stonecutter configurations.
 * Available in [StonecutterBuild] and [StonecutterController].
 */
interface StonecutterUtility {
    /**
     * Parses both parameters as [SemanticVersion] and compares them.
     *
     * @param left Version on the left side of the comparison
     * @param right Version on the right side of the comparison
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    fun compare(left: String, right: String) =
        VersionParser.parse(left).value.compareTo(VersionParser.parse(right).value)

    /**
     * Parses both parameters as [SemanticVersion] or [StringVersion] and compares them.
     *
     * @param left Version on the left side of the comparison
     * @param right Version on the right side of the comparison
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    @ApiStatus.Experimental
    fun compareLenient(left: String, right: String) =
        VersionParser.parseLenient(left).value.compareTo(VersionParser.parse(right).value)

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
    @Contract(pure = true)
    @Deprecated("Use the non-infix variant", ReplaceWith("compare(this, other)"))
    infix fun String.comp(other: String) = compare(this, other)

    /**
     * Evaluates the passed version as [SemanticVersion] and compares to the given predicate(s).
     *
     * @param version Version to test against
     * @param predicate One or multiple version predicates separated with spaces. Predicates may have an operator (=, >, <=, ~, etc.; defaults to =), followed by a version
     * @return `true` if all predicates succeed
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    fun eval(version: String, predicate: String): Boolean {
        val target = VersionParser.parse(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }

    /**
     * Evaluates the passed version as [SemanticVersion] or [StringVersion] and compares to the given predicate(s).
     *
     * @param version Version to test against
     * @param predicate One or multiple version predicates separated with spaces. Predicates may have an operator (=, >, <=, ~, etc; defaults to =), followed by a version
     * @return `true` if all predicates succeed
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    @ApiStatus.Experimental
    fun evalLenient(version: String, predicate: String): Boolean {
        val target = VersionParser.parseLenient(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }
}