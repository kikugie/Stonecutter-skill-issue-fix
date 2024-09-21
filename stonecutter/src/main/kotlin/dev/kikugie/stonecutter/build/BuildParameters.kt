package dev.kikugie.stonecutter.build

import dev.kikugie.semver.Version
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.process.FileFilter
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Parameters passed to the file processor and the Intellij plugin.
 */
@Serializable
data class BuildParameters(
    val constants: MutableMap<String, Boolean> = mutableMapOf(),
    val swaps: MutableMap<String, String> = mutableMapOf(),
    val dependencies: MutableMap<String, Version> = mutableMapOf(),
    val excludedExtensions: MutableSet<String> = mutableSetOf(
        "png", "jpg", "jpeg", "webp", "gif", "svg",
        "mp3", "wav", "ogg",
        "DS_Store", // Mac momentos
    ),
    val excludedPaths: MutableSet<Path> = mutableSetOf()
) {
    fun toTransformParams(version: String, key: String = "minecraft"): TransformParameters = with(dependencies) {
        getOrElse(key) { VersionParser.parseLenient(version) }.let {
            put(key, it)
            put("", it)
        }
        TransformParameters(swaps, constants, this)
    }

    fun toFileFilter() = FileFilter(excludedExtensions, excludedPaths)
}