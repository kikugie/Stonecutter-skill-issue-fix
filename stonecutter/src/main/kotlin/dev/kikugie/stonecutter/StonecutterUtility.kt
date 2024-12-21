package dev.kikugie.stonecutter

import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import org.jetbrains.annotations.Contract

// link: wiki-eval
/**
 * Provides a set of pure functions to ease Stonecutter configurations.
 * Available in [StonecutterBuild] and [StonecutterController].
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions">Wiki page</a>
 */
interface StonecutterUtility {
    // link: wiki-eval
    /**
     * Evaluates the passed version as [SemanticVersion] and compares to the given predicate(s),
     * which have to be separated by a space.
     * If the passed version is invalid [VersionParsingException] will be thrown
     *
     * @sample stonecutter_samples.eval.strict
     * @throws VersionParsingException
     * @see VersionParser.parsePredicate
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions">Wiki page</a>
     */
    @Contract(pure = true) @StonecutterAPI
    fun eval(version: SemanticVersion, predicate: String): Boolean {
        val target = VersionParser.parse(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }

    // link: wiki-eval
    /**
     * Evaluates the passed version as [SemanticVersion] or [AnyVersion] and compares to the given predicate(s).
     *
     * @sample stonecutter_samples.eval.lenient
     * @see VersionParser.parsePredicateLenient
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions">Wiki page</a>
     */
    @Contract(pure = true) @StonecutterAPI
    fun evalLenient(version: AnyVersion, predicate: String): Boolean {
        val target = VersionParser.parseLenient(version).value
        return predicate.split(' ').all {
            VersionParser.parsePredicateLenient(it).value.eval(target)
        }
    }
}