package dev.kikugie.stonecutter.configuration

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * Parameters used by the file processor.
 */
@Serializable
sealed interface StonecutterDataView {
    val debug: Boolean
    val constants: Map<String, Boolean>
    val swaps: Map<String, String>
    val dependencies: Map<String, SemanticVersion>
    val excludedExtensions: Set<String>
    val excludedPaths: Set<Path>
}

/**
 * Defaulted mutable implementation of [StonecutterDataView]
 */
@Serializable
data class StonecutterData(
    override var debug: Boolean = false,
    override var constants: MutableMap<String, Boolean> = mutableMapOf(),
    override var swaps: MutableMap<String, String> = mutableMapOf(),
    override var dependencies: MutableMap<String, SemanticVersion> = mutableMapOf(),
    override var excludedExtensions: MutableSet<String> = mutableSetOf(
        "png", "jpg", "jpeg", "webp", "gif", "svg",
        "mp3", "wav", "ogg",
        "DS_Store", // Mac momentos
        ),
    override var excludedPaths: MutableSet<Path> = mutableSetOf()
) : StonecutterDataView
