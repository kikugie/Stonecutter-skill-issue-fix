@file:UseSerializers(RegexSerializer::class)

package dev.kikugie.stonecutter.data.parameters

import dev.kikugie.semver.Version
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.build.BuildAbstraction
import dev.kikugie.stonecutter.data.RegexSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * Represents the build parameters used by the file processor.
 *
 * @property constants Constant values set by [BuildAbstraction.const]
 * @property swaps Swap replacements set by [BuildAbstraction.swap]
 * @property dependencies Dependency versions set by [BuildAbstraction.dependency]
 */
@Serializable
data class BuildParameters(
    val constants: MutableMap<Identifier, Boolean> = mutableMapOf(),
    val swaps: MutableMap<Identifier, String> = mutableMapOf(),
    val dependencies: MutableMap<Identifier, Version> = mutableMapOf(),
    val replacements: ReplacementData = ReplacementData(),
    val extensions: MutableSet<String> = mutableSetOf("java", "kt", "kts", "groovy", "gradle", "scala", "sc", "json5", "hjson"),
    val exclusions: MutableSet<String> = mutableSetOf()
) {
    @Serializable
    class ReplacementData {
        val basic: MutableList<ReplacementGraph> = mutableListOf()
        val regex: MutableList<ReplacementRegex> = mutableListOf()

        internal fun basic(from: String, to: String) {
            var match = false
            for (it in basic) when {
                from == it.target ->
                    if (to in it.sources) fail("Entries '%s' and '%s' create a circular dependency:\n%s"
                        .format(from, to, underline(it.toString(), from)))
                    else { it.target = to; it.sources += from; match = true }
                to == it.target || to in it.sources -> kotlin.run { it.sources += from; match = true }
                from in it.sources -> fail("Entry '%s' can be replaced by both '%s' and '%s':\n%s"
                    .format(from, to, it.target, underline(it.toString(), from)))
            }
            if (!match) basic += ReplacementGraph(from, to)
        }

        internal fun regex(from: String, to: String) {
            regex += ReplacementRegex(Regex(from), to)
        }

        private fun fail(message: String): Nothing = throw IllegalArgumentException(message)
        private fun underline(s: String, match: String): String {
            val start = s.indexOf(match)
            val tildes = " ".repeat(start) + "~".repeat(match.length)
            return "$s\n$tildes"
        }
    }

    @Serializable
    class ReplacementGraph(var target: String) {
        constructor(source: String, target: String) : this(target) {
            sources += source
        }
        val sources: MutableSet<String> = mutableSetOf()
        override fun toString(): String = "[${sources.joinToString { "'$it'" }}] -> '$target'"
    }

    @Serializable
    data class ReplacementRegex(val regex: Regex, val replacement: String)
}