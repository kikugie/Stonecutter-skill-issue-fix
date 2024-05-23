package dev.kikugie.version.parse

import dev.kikugie.version.impl.SemanticVersion
import dev.kikugie.version.exception.VersionParsingException

/**
 * Semantic version parser based on the Fabric Loader implementation, but doesn't allow `.x` wildcards.
 *
 * @see <a href="https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/util/version/SemanticVersionImpl.java">SemanticVersionImpl</a>
 */
object SemanticVersionParser : Parser<SemanticVersion> {
    override fun parse(input: CharSequence): SemanticVersion {
        var version = input.toString()
        var preModifier = ""
        var postModifier = ""

        val plusDelimeter = version.indexOf('+')
        if (plusDelimeter >= 0) {
            postModifier = version.substring(plusDelimeter + 1)
            version = version.substring(0, plusDelimeter)
        }

        val dashDelimeter = version.indexOf('-')
        if (dashDelimeter >= 0) {
            preModifier = version.substring(dashDelimeter + 1)
            version = version.substring(0, dashDelimeter)
        }

        if (version.startsWith('.') || version.endsWith('.'))
            throw VersionParsingException("Incomplete version number: $version in $input")
        if (preModifier.isNotEmpty() && !preModifier.validate())
            throw VersionParsingException("Invalid metadata: $preModifier in $input")

        val stringComponents = version.split('.')
        if (stringComponents.isEmpty())
            throw VersionParsingException("Empty version number: $version in $input")
        val components = IntArray(stringComponents.size)

        stringComponents.forEachIndexed { i, it ->
            if (it.isEmpty())
                throw VersionParsingException("Incomplete version number: $version in $input")
            components[i] = try {
                it.toUInt().toInt()
            } catch (e: NumberFormatException) {
                throw VersionParsingException("Invalid version number: $version in $input").apply {
                    initCause(e)
                }
            }
        }
        return SemanticVersion(components, preModifier, postModifier)
    }

    private fun String.validate(): Boolean {
        val segments = split('.')
        if (segments.isEmpty()) return false
        for (str in segments) {
            if (str.isEmpty()) return false
            for (char in str)
                if (char.isLetterOrDigit() || char == '-')
                    return false
        }
        return true
    }
}