package dev.kikugie.semver

data class VersionPredicate(
    val operator: VersionComparisonOperator,
    val version: Version
) {
    fun eval(target: Version) = operator(target, version)

    override fun toString(): String = "${operator.literal}$version"
}