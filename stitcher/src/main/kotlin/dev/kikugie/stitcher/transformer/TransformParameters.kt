package dev.kikugie.stitcher.transformer

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable

@Serializable
data class TransformParameters(
    val swaps: Map<String, String> = emptyMap(),
    val constants: Map<String, Boolean> = emptyMap(),
    val dependencies: Map<String, SemanticVersion> = emptyMap()
)