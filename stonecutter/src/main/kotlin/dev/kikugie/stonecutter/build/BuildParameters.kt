package dev.kikugie.stonecutter.build

import dev.kikugie.semver.Version
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.process.FileFilter
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Represents the build parameters used by the file processor.
 *
 * @property constants Constant values set by [BuildConfiguration.const]
 * @property swaps Swap replacements set by [BuildConfiguration.swap]
 * @property dependencies Dependency versions set by [BuildConfiguration.dependency]
 * @property excludedExtensions Extensions excluded from processing by [BuildConfiguration.exclude].
 * By default, excludes common image and audio files.
 * @property excludedPaths Files excluded from processing by [BuildConfiguration.exclude]
 */
@Serializable
data class BuildParameters(
    val constants: MutableMap<Identifier, Boolean> = mutableMapOf(),
    val swaps: MutableMap<Identifier, String> = mutableMapOf(),
    val dependencies: MutableMap<Identifier, Version> = mutableMapOf(),
    val excludedExtensions: MutableSet<String> = mutableSetOf(
        "png", "jpg", "jpeg", "webp", "gif", "svg",
        "mp3", "wav", "ogg",
        "DS_Store", // Mac momentos
    ),
    val excludedPaths: MutableSet<Path> = mutableSetOf()
) {
    /**Creates parameters used by the file processor.*/
    internal fun toTransformParams(version: String, key: String = "minecraft"): TransformParameters = with(dependencies) {
        getOrElse(key) { VersionParser.parseLenient(version).value }.let {
            put(key, it)
            put("", it)
        }
        TransformParameters(swaps, constants, this)
    }

    /**Creates a [FileFilter] from the specified excluded paths and extensions.*/
    internal fun toFileFilter() = FileFilter(excludedExtensions, excludedPaths)
}