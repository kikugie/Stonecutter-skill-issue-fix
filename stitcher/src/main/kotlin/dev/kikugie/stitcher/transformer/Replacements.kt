@file:UseSerializers(RegexSerializer::class)

package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.util.RegexSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

object Replacements {
    @Serializable
    /**Represents the stage a replacement is executed.*/
    enum class ReplacementPhase {
        /**Replaces values before the contents are parsed.*/
        FIRST,
        /**Replaces values after versioned comments have been evaluated and reassembled.*/
        LAST;

        /**Runs replacers if their phase matches the current one.*/
        fun replace(text: CharSequence, data: ReplacementData): CharSequence = StringBuilder(text)
            .replace(data.replacements.filter { it.phase == this })
            .replace(data.regexes.filter { it.phase == this })
    }

    @Serializable
    data class ReplacementData(
        val replacements: List<StringReplacement> = emptyList(),
        val regexes: List<RegexReplacement> = emptyList()
    )

    @Serializable
    data class StringReplacement(val phase: ReplacementPhase, val sources: List<String>, val target: String)

    @Serializable
    data class RegexReplacement(val phase: ReplacementPhase, val regex: Regex, val target: String)

    private fun StringBuilder.replace(entries: List<StringReplacement>): StringBuilder {
        if (isEmpty() || entries.isEmpty()) return this
        val lookup = entries.flatMap { e -> e.sources.map { it to e.target } }.toMap()
        for ((key, value) in lookup) {
            var index = indexOf(key)
            while (index >= 0) {
                replace(index, index + key.length, value)
                index += value.length
                index = indexOf(key, index)
            }
        }
        return this
    }

    private fun StringBuilder.replace(entries: List<RegexReplacement>): StringBuilder {
        if (isEmpty() || entries.isEmpty()) return this
        var string = ""
        for ((_, regex, target) in entries) string = regex.replace(this, target)
        return StringBuilder(string)
    }
}