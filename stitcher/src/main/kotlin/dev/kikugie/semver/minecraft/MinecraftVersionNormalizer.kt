package dev.kikugie.semver.minecraft

import dev.kikugie.semver.SemanticVersion
import dev.kikugie.semver.SemanticVersionParser

class MinecraftVersionNormalizer(private val supplier: MinecraftVersionInfo) {
    fun parse(input: CharSequence): SemanticVersion =
        SemanticVersionParser.parse(getValidVersion(input.toString()))

    private fun getValidVersion(version: String): String {
        if (version.isRelease()) // Full releases like 1.19 or 1.20
            return version
        asSpecialVersion(version)?.run { // Special versions, matched output can be mapped directly
            return this
        }
        tryNormalizeSnapshot(version)?.run { // Snapshots like 24w05b
            return this
        }
        var (release, modifier) = version.split('-', limit = 2)
        if (modifier.startsWith("rc")) {
            var build = modifier.substringAfter("rc")
            if (release == "1.16") build = "${build.toInt() + 8}"
            modifier = "rc.$build"
        } else if (modifier.startsWith("pre")) {
            val build = modifier.substringAfter("pre")
            val legacy = release.split('.')[1].toInt() <= 16
            modifier = if (legacy) "rc.$build" else "beta.$build"
        }
        return "$release-$modifier"
    }

    private fun tryNormalizeSnapshot(str: String): String? = try {
        val year = str.substring(0..1).toInt()
        val week = str.substring(3..4).toInt()
        val index = str.substring(5..<str.length)
        val version = findMatchingRelease(str)
        if (version == null) null
        else "$version-alpha.$year.$week.$index"
    } catch (e: Exception) {
        null
    }

    private fun findMatchingRelease(version: String): String? {
        var pending = supplier.nextRelease
        for (ver in supplier.versions) {
            if (ver.isRelease()) pending = ver
            if (ver == version) return pending
        }
        return null
    }

    private fun asSpecialVersion(str: String): String? = when (str) {
        "13w12~" -> "1.5.1-alpha.13.12.a"
        "15w14a" -> "1.8.4-alpha.15.14.a+loveandhugs"
        "1.RV-Pre1" -> "1.9.2-rv+trendy"
        "24w14potato" -> "1.20.5-alpha.24.12.potato"
        "23w13a_or_b" -> "1.20-alpha.23.13.ab"
        "3D Shareware v1.34" -> "1.14-alpha.19.13.shareware"
        "20w14~" -> "1.16-alpha.20.13.inf"
        "1.14.3 - Combat Test" -> "1.14.3-rc.4.combat.1"
        "Combat Test 2" -> "1.14.5-combat.2"
        "Combat Test 3" -> "1.14.5-combat.3"
        "Combat Test 4" -> "1.15-rc.3.combat.4"
        "Combat Test 5" -> "1.15.2-rc.2.combat.5"
        "Combat Test 6" -> "1.16.2-beta.3.combat.6"
        "Combat Test 7" -> "1.16.3-combat.7"
        "1.16_combat-2" -> "1.16.3-combat.7.b"
        "1.16_combat-3" -> "1.16.3-combat.7.c"
        "1.16_combat-4" -> "1.16.3-combat.8"
        "1.16_combat-5" -> "1.16.3-combat.8.b"
        "1.16_combat-6" -> "1.16.3-combat.8.c"
        else -> null
    }

    private fun String.isRelease(): Boolean {
        val components = split('.').toMutableList()
        if (components.size == 2) components += "0"
        else if (components.size > 3 || components.size < 2) return false
        return components.all { it.isNotBlank() && it.all(Char::isDigit) }
    }
}