package dev.kikugie.stonecutter.data

import dev.kikugie.semver.Version
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.StonecutterProject
import kotlinx.serialization.Serializable
import java.nio.file.Path

typealias Nodes = MutableSet<StonecutterProject>
typealias NodeMap = MutableMap<ProjectName, Nodes>

@Serializable
data class TreeModel(
    val nodes: NodeMap,
    val versions: Set<StonecutterProject>,
    val vcsVersion: StonecutterProject
)

@Serializable
data class BranchModel(
    val versions: List<StonecutterProject>,
    val vcsVersion: StonecutterProject,
    val current: StonecutterProject,
)

/**
 * Parameters passed to the file processor and the Intellij plugin.
 */
@Serializable
data class StitcherParameters(
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