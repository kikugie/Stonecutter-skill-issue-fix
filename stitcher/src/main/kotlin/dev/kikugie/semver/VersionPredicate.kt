package dev.kikugie.semver

data class VersionPredicate(
    val operator: VersionComparisonOperator,
    val version: SemanticVersion
)