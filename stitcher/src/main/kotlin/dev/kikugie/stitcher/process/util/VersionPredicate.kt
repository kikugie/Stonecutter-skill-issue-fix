package dev.kikugie.stitcher.process.util

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.semver.VersionComparisonOperator

data class VersionPredicate(
    val operator: VersionComparisonOperator,
    val version: SemanticVersion
)