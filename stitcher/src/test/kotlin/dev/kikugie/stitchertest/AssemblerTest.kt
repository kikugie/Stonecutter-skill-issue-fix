package dev.kikugie.stitchertest

import dev.kikugie.stitcher.assembler.AssemblyVisitor
import dev.kikugie.stitchertest.util.parse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object AssemblerTest {
    @TestFactory
    fun `test assembler`(): List<DynamicTest> {
        val samples = mutableListOf<Pair<String, String>>()
        ParserTest.tests.mapTo(samples) {
            it.key to it.value.first
        }
        ScannerTest.tests.mapTo(samples) {
            it.key to it.value.first
        }
        return samples.map {
            DynamicTest.dynamicTest(it.first) {
                assertEquals(AssemblyVisitor.visitScope(it.second.parse()), it.second)
            }
        }
    }
}