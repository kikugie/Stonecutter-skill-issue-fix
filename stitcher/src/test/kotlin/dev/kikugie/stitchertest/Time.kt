package dev.kikugie.stitchertest

import dev.kikugie.semver.SemanticVersionParser
import org.junit.jupiter.api.Test

object Time {
    @Test
    fun test() {
        val version1 = SemanticVersionParser.parse("1.21")
        val version2 = SemanticVersionParser.parse("1.21-pre.1")
        println(version1.compareTo(version2))
    }
}