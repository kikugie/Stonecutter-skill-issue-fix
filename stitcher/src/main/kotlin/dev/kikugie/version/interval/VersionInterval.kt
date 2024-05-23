package dev.kikugie.version.interval

import dev.kikugie.version.impl.SemanticVersion
import dev.kikugie.version.impl.Version

class VersionInterval(
    val min: Version?,
    val minInclusive: Boolean,
    val max: Version?,
    val maxInclusive: Boolean,
) {
    val isSemantic: Boolean get() = (min == null || min is SemanticVersion) && (max == null || max is SemanticVersion)

    companion object {
        val INFINITE = VersionInterval(null, false, null, false)
    }
}