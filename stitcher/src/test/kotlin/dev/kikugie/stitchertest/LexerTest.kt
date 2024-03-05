package dev.kikugie.stitchertest

import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.Comment.*
import dev.kikugie.stitcher.type.StitcherToken.*
import dev.kikugie.stitchertest.util.token
import dev.kikugie.stitchertest.util.tokenize
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object LexerTest {
    val tests = buildMap {
        this["base tokens"] = "/*? { } ( ) ! || && if else an expression */" to buildList {
            token("/*", 0..<2, COMMENT_START)
            token("?", 2..<3, CONDITION)
            token("{", 4..<5, SCOPE_OPEN)
            token("}", 6..<7, SCOPE_CLOSE)
            token("(", 8..<9, GROUP_OPEN)
            token(")", 10..<11, GROUP_CLOSE)
            token("!", 12..<13, NEGATE)
            token("||", 14..<16, OR)
            token("&&", 17..<19, AND)
            token("if", 20..<22, IF)
            token("else", 23..<27, ELSE)
            token("an expression", 28..<41, EXPRESSION)
            token("*/", 42..<44, COMMENT_END)
        }
    }

    fun check(input: String, expected: List<Token>) =
        assertEquals(expected.asSequence().yaml(), input.tokenize().toList().dropLast(1).yaml())

    @TestFactory
    fun `test lexer`() =
        tests.map { DynamicTest.dynamicTest(it.key) { check(it.value.first, it.value.second) } }
}