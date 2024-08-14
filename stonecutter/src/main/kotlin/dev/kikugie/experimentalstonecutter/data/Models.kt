package dev.kikugie.experimentalstonecutter.data

import dev.kikugie.experimentalstonecutter.settings.NodeMap
import dev.kikugie.semver.Version
import dev.kikugie.experimentalstonecutter.StonecutterProject
import kotlinx.serialization.Serializable
import java.nio.file.Path

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

@Serializable
data class BuildData(
    val constants: MutableMap<String, Boolean> = mutableMapOf(),
    val swaps: MutableMap<String, String> = mutableMapOf(),
    val dependencies: MutableMap<String, Version> = mutableMapOf(),
    val excludedExtensions: MutableSet<String> = mutableSetOf(
        "png", "jpg", "jpeg", "webp", "gif", "svg",
        "mp3", "wav", "ogg",
        "DS_Store", // Mac momentos
    ),
    val excludedPaths: MutableSet<Path> = mutableSetOf()
)