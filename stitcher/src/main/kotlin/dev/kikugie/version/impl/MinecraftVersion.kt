package dev.kikugie.version.impl

import dev.kikugie.version.exception.VersionParsingException
import dev.kikugie.version.parse.MinecraftVersionNormalizer

@Suppress("MemberVisibilityCanBePrivate", "unused")
data class MinecraftVersion(val content: String, val supplier: VersionSupplier) : Version {
    data class VersionSupplier(
        val versions: List<String>,
        val nextRelease: String,
    )

    val semver: SemanticVersion? by lazy {
        try {
            MinecraftVersionNormalizer(supplier).parse(content)
        } catch (_: VersionParsingException) {
            null
        }
    }

    override fun compareTo(other: Version) = (if (semver != null) when (other) {
        is MinecraftVersion ->
            if (other.semver == null) null
            else semver!!.compareTo(other.semver!!)

        is SemanticVersion -> semver!!.compareTo(other)
        else -> null
    } else null) ?: content.compareTo(other.toString())

    override fun toString(): String = content
}