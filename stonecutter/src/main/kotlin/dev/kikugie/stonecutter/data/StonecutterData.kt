package dev.kikugie.stonecutter.data

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class StonecutterData(
    var debug: Boolean = false,
    val constants: MutableMap<String, Boolean> = mutableMapOf(),
    val swaps: MutableMap<String, String> = mutableMapOf(),
    val dependencies: MutableMap<String, SemanticVersion> = mutableMapOf(),
    val excludedExtensions: MutableSet<String> = mutableSetOf(
        "png", "jpg", "jpeg", "webp", "gif", "svg",
        "mp3", "wav", "ogg",
        "DS_Store", // Mac momentos
    ),
    val excludedPaths: MutableSet<Path> = mutableSetOf()
)