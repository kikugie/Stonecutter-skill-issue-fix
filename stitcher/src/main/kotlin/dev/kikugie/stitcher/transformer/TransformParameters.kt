package dev.kikugie.stitcher.transformer

import dev.kikugie.semver.Version
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.Replacements.ReplacementData
import kotlinx.serialization.Serializable

@Serializable
data class TransformParameters(
    val swaps: Map<String, String> = emptyMap(),
    val constants: Map<String, Boolean> = emptyMap(),
    val dependencies: Map<String, Version> = emptyMap(),
    val replacements: ReplacementData = ReplacementData()
) {
    companion object {
        inline fun TransformParameters(build: TransformParametersBuilder.() -> Unit) =
            TransformParametersBuilder().apply(build).build()
    }

    class TransformParametersBuilder {
        val swaps: MutableMap<String, String> = mutableMapOf()
        val constants: MutableMap<String, Boolean> = mutableMapOf()
        val dependencies: MutableMap<String, String> = mutableMapOf()
        val replacements: MutableList<Replacements.StringReplacement> = mutableListOf()
        val regexes: MutableList<Replacements.RegexReplacement> = mutableListOf()

        fun build() = TransformParameters(
            swaps.toMap(),
            constants.toMap(),
            dependencies.mapValues { VersionParser.parse(it.value).value },
            ReplacementData(replacements.toList(), regexes.toList())
        )
    }
}