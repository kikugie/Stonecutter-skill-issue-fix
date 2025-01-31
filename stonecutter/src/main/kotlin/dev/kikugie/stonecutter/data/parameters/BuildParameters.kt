@file:UseSerializers(RegexSerializer::class)

package dev.kikugie.stonecutter.data.parameters

import dev.kikugie.semver.Version
import dev.kikugie.stitcher.transformer.Replacements.RegexReplacement
import dev.kikugie.stitcher.transformer.Replacements.ReplacementPhase
import dev.kikugie.stitcher.transformer.Replacements.StringReplacement
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.build.*
import dev.kikugie.stitcher.util.RegexSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.text.Regex

/**
 * Represents the build parameters used by the file processor.
 *
 * @property constants Constant values set by [ConstantVariants]
 * @property swaps Swap replacements set by [SwapVariants]
 * @property dependencies Dependency versions set by [DependencyVariants]
 * @property replacements String and regex replacement entries set by [ReplacementVariants]
 * @property extensions Set of file formats allowlisted for file processing by [FilterVariants]
 * @property exclusions Set of individual files excluded from processing by [FilterVariants]
 */
@Serializable
public data class BuildParameters(
    val constants: MutableMap<Identifier, Boolean> = mutableMapOf(),
    val swaps: MutableMap<Identifier, String> = mutableMapOf(),
    val dependencies: MutableMap<Identifier, Version> = mutableMapOf(),
    val replacements: ReplacementData = ReplacementData(),
    val extensions: MutableSet<String> = mutableSetOf("java", "kt", "kts", "groovy", "gradle", "scala", "sc", "json5", "hjson"),
    val exclusions: MutableSet<String> = mutableSetOf()
) {
    @Serializable
    public class ReplacementData {
        internal val string: MutableList<ReplacementGraph> = mutableListOf()
        internal val regex: MutableList<RegexReplacement> = mutableListOf()

        internal fun string(phase: ReplacementPhase, from: String, to: String) {
            var match = false
            for (it in string) when {
                it.phase != phase -> continue
                from == it.target ->
                    if (to in it.sources) fail("Entries '%s' and '%s' create a circular dependency:\n%s"
                        .format(from, to, underline(it.toString(), from)))
                    else { it.target = to; it.sources += from; match = true }
                to == it.target || to in it.sources -> kotlin.run { it.sources += from; match = true }
                from in it.sources -> fail("Entry '%s' can be replaced by both '%s' and '%s':\n%s"
                    .format(from, to, it.target, underline(it.toString(), from)))
            }
            if (!match) string += ReplacementGraph(phase, from, to)
        }

        internal fun regex(phase: ReplacementPhase, from: String, to: String) {
            regex += RegexReplacement(phase, Regex(from), to)
        }

        private fun fail(message: String): Nothing = throw IllegalArgumentException(message)
        private fun underline(s: String, match: String): String {
            val start = s.indexOf(match)
            val tildes = " ".repeat(start) + "~".repeat(match.length)
            return "$s\n$tildes"
        }
    }

    @Serializable
    internal class ReplacementGraph(val phase: ReplacementPhase, var target: String) {
        constructor(phase: ReplacementPhase, source: String, target: String) : this(phase, target) {
            sources += source
        }
        val sources: MutableSet<String> = mutableSetOf()
        fun lock() = StringReplacement(phase, sources.toList(), target)
        override fun toString(): String = "[${sources.joinToString { "'$it'" }}] -> '$target'"
    }
}