package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitcher.type.Comment.COMMENT_END
import dev.kikugie.stitcher.type.Comment.COMMENT_START
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
            token("/*", COMMENT_START)
            token("?", CONDITION)
            token("{", SCOPE_OPEN)
            token("}", SCOPE_CLOSE)
            token("(", GROUP_OPEN)
            token(")", GROUP_CLOSE)
            token("!", NEGATE)
            token("||", OR)
            token("&&", AND)
            token("if", IF)
            token("else", ELSE)
            token("an expression", EXPRESSION)
            token("*/", COMMENT_END)
        }
    }

    fun check(input: String, expected: List<Token>) =
        assertEquals(expected.asSequence().yaml(), input.tokenize().toList().dropLast(1).yaml())

    @TestFactory
    fun `test lexer`() =
        tests.map { DynamicTest.dynamicTest(it.key) { check(it.value.first, it.value.second) } }
}