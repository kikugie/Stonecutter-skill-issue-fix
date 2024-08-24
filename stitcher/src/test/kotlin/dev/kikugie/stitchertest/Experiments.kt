package dev.kikugie.stitchertest

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.semver.VersionParsingException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

object Experiments {
    private inline fun <reified T : Throwable> assertThrows(crossinline block: () -> Unit) = assertThrows(T::class.java) { block() }

    @Test
    fun test() {
        assertThrows<VersionParsingException> { SemanticVersionParser.parse("1.2.3-...") }
    }
}