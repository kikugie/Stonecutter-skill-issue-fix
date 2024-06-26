package dev.kikugie.semver

import dev.kikugie.semver.VersionComparisonOperator.Companion.match
import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength

data class VersionPredicate(
    val operator: VersionComparisonOperator,
    val version: SemanticVersion
) {
    fun eval(target: SemanticVersion) = operator(target, version)

    companion object {
        @Throws(VersionParsingException::class)
        fun parse(predicate: String): VersionPredicate {
            val len = predicate.operatorLength()
            val op = match(predicate.substring(0, len))
            val ver = SemanticVersionParser.parse(predicate.substring(len))
            return VersionPredicate(op, ver)
        }
    }
}