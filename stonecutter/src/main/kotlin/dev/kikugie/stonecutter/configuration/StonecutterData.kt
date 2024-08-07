package dev.kikugie.stonecutter.configuration

import dev.kikugie.semver.Version
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Parameters used by the file processor.
 */
@Serializable
data class StonecutterData(
    var debug: Boolean = false,
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