package dev.kikugie.stonecutter.controller

import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.lexer.IdentifierRecognizer
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.BuildConfiguration
import dev.kikugie.stonecutter.data.StitcherParameters
import java.nio.file.Path

/**
 * Stores parameters configured in [StonecutterController.configureAll].
 *
 * @property branch Currently processed branch
 * @property version Currently processed version.
 * **May not exist in the given branch**, but you should still provide the same set of parameters.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ParameterHolder(
    val branch: ProjectBranch,
    val version: StonecutterProject
) : BuildConfiguration {
    internal val data = StitcherParameters()

    override fun swap(identifier: String, replacement: String) {
        data.swaps[validateId(identifier)] = replacement
    }

    override fun const(identifier: String, value: Boolean) {
        data.constants[validateId(identifier)] = value
    }

    override fun dependency(identifier: String, version: String) {
        data.dependencies[validateId(identifier)] = validateSemver(version)
    }

    override fun exclude(path: Path) {
        data.excludedPaths.add(path)
    }

    override fun exclude(path: String) {
        require(path.isNotBlank()) { "Path must not be empty" }
        if (path.startsWith("*.")) data.excludedExtensions.add(path.substring(2))
        else data.excludedPaths.add(branch.file(path).toPath())
    }

    private fun validateId(id: String) = id.apply {
        require(all(IdentifierRecognizer.Companion::allowed)) { "Invalid identifier: $this" }
    }

    private fun validateSemver(version: String) = try {
        VersionParser.parse(version)
    } catch (e: VersionParsingException) {
        throw IllegalArgumentException("Invalid semantic version: $version").apply {
            initCause(e)
        }
    }
}