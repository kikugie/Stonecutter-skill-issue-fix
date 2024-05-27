package dev.kikugie.stitcher.process

import dev.kikugie.semver.SemanticVersion
import kotlinx.serialization.Serializable

@Serializable
data class TransformParameters(
    val swaps: Map<String, String>,
    val constants: Map<String, Boolean>,
    val dependencies: Map<String, SemanticVersion>
)