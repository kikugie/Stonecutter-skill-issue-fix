package dev.kikugie.stonecutter.configuration

import dev.kikugie.semver.SemanticVersion
import java.nio.file.Path

interface StonecutterDataView {
    val debug: Boolean
    val constants: Map<String, Boolean>
    val swaps: Map<String, String>
    val dependencies: Map<String, SemanticVersion>
    val excludedExtensions: Set<String>
    val excludedPaths: Set<Path>
}

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
