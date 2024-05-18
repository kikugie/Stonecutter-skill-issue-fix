package dev.kikugie.stitchertest

import dev.kikugie.stitcher.exception.SyntaxException
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

object ParserTest {
    private fun check(input: String, expected: String) =
        assertEquals(expected, input.parse().yaml())

    @TestFactory
    fun `test parser`() =
        PARSER_TESTS.map { DynamicTest.dynamicTest(it.left) { check(it.middle, it.right) } }

    @Test
    fun `cross-closing`() {
        val input = """
        //? bool {
        //$ token {
        //?}
        //$}
        """.trimIndent()
        assertThrows<SyntaxException>(input::parse)
    }
}