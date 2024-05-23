package dev.kikugie.version.operator

import dev.kikugie.version.impl.SemanticVersion
import dev.kikugie.version.impl.Version

enum class VersionComparisonOperator(
    val literal: String,
    val minInclusive: Boolean,
    val maxInclusive: Boolean
) : (Version, Version) -> Boolean {
    GREATER_EQUAL(">=", true, false) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 >= p2
        override fun minVersion(version: SemanticVersion): SemanticVersion = version
    },
    LESS_EQUAL("<=", false, true) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 <= p2
        override fun maxVersion(version: SemanticVersion): SemanticVersion = version
    },
    GREATER(">", false, false) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 > p2
        override fun minVersion(version: SemanticVersion): SemanticVersion = version
    },
    LESS("<", false, false) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 < p2
        override fun maxVersion(version: SemanticVersion): SemanticVersion = version
    },
    EQUAL("=", true, true) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1.compareTo(p2) == 0
        override fun minVersion(version: SemanticVersion): SemanticVersion = version
        override fun maxVersion(version: SemanticVersion): SemanticVersion = version
    },
    SAME_TO_NEXT_MINOR("~", true, false) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean =
            p1 >= p2 && p1[0] == p2[0] && p1[1] == p2[1]

        override fun minVersion(version: SemanticVersion): SemanticVersion = version
        override fun maxVersion(version: SemanticVersion): SemanticVersion =
            SemanticVersion(intArrayOf(version[0] ?: 0, (version[1] ?: 0) + 1),"", "")
    },
    SAME_TO_NEXT_MAJOR("", true, false) {
        override fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean =
            p1 >= p2 && p1[0] == p2[0]

        override fun minVersion(version: SemanticVersion): SemanticVersion = version
        override fun maxVersion(version: SemanticVersion): SemanticVersion =
            SemanticVersion(intArrayOf((version[0] ?: 0) + 1),"", "")
    };

    abstract fun test(p1: SemanticVersion, p2: SemanticVersion): Boolean
    open fun minVersion(version: SemanticVersion): SemanticVersion? = null
    open fun maxVersion(version: SemanticVersion): SemanticVersion? = null

    override fun invoke(p1: Version, p2: Version): Boolean =
        if (p1 is SemanticVersion && p2 is SemanticVersion)
            test(p1, p2)
        else if (minInclusive || maxInclusive)
            p1.toString() == p2.toString()
        else false
}