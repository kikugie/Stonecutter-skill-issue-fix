package dev.kikugie.semver

import dev.kikugie.semver.VersionComparisonOperator.Companion.match
import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength

data class VersionPredicate(
    val operator: VersionComparisonOperator,
    val version: Version
) {
    fun eval(target: Version) = operator(target, version)

    override fun toString(): String = "${operator.literal}$version"

    companion object {
        @Throws(VersionParsingException::class)
        fun parse(predicate: String): VersionPredicate {
            val len = predicate.operatorLength()
            val op = match(predicate.substring(0, len))
            val ver = VersionParser.parse(predicate.substring(len))
            return VersionPredicate(op, ver)
        }

        @Throws(VersionParsingException::class)
        fun parseLenient(predicate: String): VersionPredicate {
            val len = predicate.operatorLength()
            val op = match(predicate.substring(0, len))
            val ver = VersionParser.parseLenient(predicate.substring(len))
            return VersionPredicate(op, ver)
        }
    }
}