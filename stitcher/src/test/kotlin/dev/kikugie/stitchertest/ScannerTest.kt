package dev.kikugie.stitchertest

import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.Comment.*
import dev.kikugie.stitchertest.util.scan
import dev.kikugie.stitchertest.util.token
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object ScannerTest {
    val tests = buildMap {
        this["slash comment"] = "// comment" to buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment", 2..9, COMMENT)
        }
        this["hash comment"] = "# comment" to buildList {
            token("#", 0..0, COMMENT_START)
            token(" comment", 1..8, COMMENT)
        }
        this["doc comment"] = """
        /**
         * doc comment
         */
        """.trimIndent() to buildList {
            token("/*", 0..1, COMMENT_START)
            token("*\n * doc comment\n ", 2..19, COMMENT)
            token("*/", 20..21, COMMENT_END)
        }
        this["multi comment"] = "/* comment */" to buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comment ", 2..10, COMMENT)
            token("*/", 11..12, COMMENT_END)
        }
        this["open comment"] = "/* comment" to buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comment", 2..9, COMMENT)
        }
        this["nested slash-slash comment"] = "// comment // comment" to buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment // comment", 2..20, COMMENT)
        }
        this["nested slash-hash comment"] = "// comment # comment" to buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment # comment", 2..19, COMMENT)
        }
        this["nested multi-multi comment"] = "/* comm /* comment */ ent */" to buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm /* comment ", 2..18, COMMENT)
            token("*/", 19..20, COMMENT_END)
            token(" ent */", 21..27, CONTENT)
        }
        this["nested slash-multi comment"] = "// comm /* comment */ ent" to buildList {
            token("//", 0..1, COMMENT_START)
            token(" comm /* comment */ ent", 2..24, COMMENT)
        }
        this["nested multi-slash comment"] = "/* comm // ent */" to buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm // ent ", 2..14, COMMENT)
            token("*/", 15..16, COMMENT_END)
        }
        this["quote in comment"] = "// \"cool\" comment" to buildList {
            token("//", 0..1, COMMENT_START)
            token(" \"cool\" comment", 2..16, COMMENT)
        }
        this["comment in quote"] = "\"nice // quote\"" to buildList {
            token("\"nice // quote\"", 0..14, CONTENT)
        }
        this["invalid comment quote"] = "/* comm \"ent */ wtf\"" to buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm \"ent ", 2..12, COMMENT)
            token("*/", 13..14, COMMENT_END)
            token(" wtf\"", 15..19, CONTENT)
        }
        this["invalid quote comment"] = "\"quote /* comm\" ent*/" to buildList {
            token("\"quote /* comm\" ent*/", 0..20, CONTENT)
        }
        this["single quote in doubles"] = "\" quote '\"" to buildList {
            token("\" quote '\"", 0..9, CONTENT)
        }
        this["double quote in singles"] = "' quote \"'" to buildList {
            token("' quote \"'", 0..9, CONTENT)
        }
        this["double quote in doc"] = "\"\"\" still \"quote\" \"\"\"" to buildList {
            token("\"\"\" still \"quote\" \"\"\"", 0..20, CONTENT)
        }
        this["escaped quote"] = "\" quote \\\" \"" to buildList {
            token("\" quote \\\" \"", 0..11, CONTENT)
        }
    }

    fun check(input: String, expected: List<Token>) =
        assertEquals(expected.asSequence().yaml(), input.scan().toList().dropLast(1).yaml())

    @TestFactory
    fun `test scanner`() =
        tests.map { DynamicTest.dynamicTest(it.key) { check(it.value.first, it.value.second) } }
}