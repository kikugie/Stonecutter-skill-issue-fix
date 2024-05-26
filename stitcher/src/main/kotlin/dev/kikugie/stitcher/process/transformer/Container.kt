package dev.kikugie.stitcher.process.transformer

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable

@Serializable
data class Container(
    val swaps: Map<String, String>,
    val constants: Map<String, Boolean>,
    val dependencies: Map<String, SemanticVersion>
)