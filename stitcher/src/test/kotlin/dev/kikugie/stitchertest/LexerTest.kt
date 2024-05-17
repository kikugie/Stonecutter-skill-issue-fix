package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object LexerTest {
    private fun check(input: String, expected: List<Token>) =
        assertEquals(expected.asSequence().yaml(), input.tokenize().toList().dropLast(1).yaml())

    @TestFactory
    fun `test lexer`() =
        LEXER_TESTS.map { DynamicTest.dynamicTest(it.left) { check(it.middle, it.right) } }
}