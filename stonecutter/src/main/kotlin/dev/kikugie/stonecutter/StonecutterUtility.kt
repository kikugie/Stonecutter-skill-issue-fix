package dev.kikugie.stonecutter

import dev.kikugie.semver.VersionParser
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract

/**
 * Provides a set of pure functions to ease Stonecutter configurations.
 * Available in [StonecutterBuild] and [StonecutterController].
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions">Wiki page</a>
 */
interface StonecutterUtility {
    /**
     * Parses both parameters as [SemanticVersion] and compares them.
     *
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    @ApiStatus.Obsolete
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @Deprecated("Use `eval()` instead", ReplaceWith("eval(left, right)"))
    fun compare(left: SemanticVersion, right: SemanticVersion) =
        VersionParser.parse(left).value.compareTo(VersionParser.parse(right).value)

    /**
     * Parses both parameters as [SemanticVersion] or [AnyVersion] and compares them.
     *
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    @ApiStatus.Obsolete
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @Deprecated("Use `evalLenient()` instead", ReplaceWith("evalLenient(left, right)"))
    fun compareLenient(left: AnyVersion, right: AnyVersion) =
        VersionParser.parseLenient(left).value.compareTo(VersionParser.parse(right).value)

    /**
     * Parses both parameters as semantic versions and compares them.
     * This function is available inside a `stonecutter` block when using Kotlin DSL.
     *
     * @return 1 if the first version is greater, -1 if the second is greater, 0 if they are equal
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Suppress("DEPRECATION")
    @Contract(pure = true)
    @ApiStatus.Obsolete
    @ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @Deprecated("Use `eval()` instead", ReplaceWith("eval(this, other)"))
    infix fun SemanticVersion.comp(other: SemanticVersion) = compare(this, other)

    /**
     * Evaluates the passed version as [SemanticVersion] and compares to the given predicate(s).
     *
     * @return `true` if all predicates succeed
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    fun eval(version: SemanticVersion, predicate: String): Boolean {
        val target = VersionParser.parse(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }

    /**
     * Evaluates the passed version as [SemanticVersion] or [AnyVersion] and compares to the given predicate(s).
     *
     * @return `true` if all predicates succeed
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/configuration.html#comparisons">Wiki</a>
     */
    @Contract(pure = true)
    fun evalLenient(version: AnyVersion, predicate: String): Boolean {
        val target = VersionParser.parseLenient(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }
}