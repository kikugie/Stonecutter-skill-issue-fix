package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.Token
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
            token("//", COMMENT_START)
            token(" comment", COMMENT)
        }
        this["hash comment"] = "# comment" to buildList {
            token("#", COMMENT_START)
            token(" comment", COMMENT)
        }
        this["doc comment"] = """
        /**
         * doc comment
         */
        """.trimIndent() to buildList {
            token("/*", COMMENT_START)
            token("*\n * doc comment\n ", COMMENT)
            token("*/", COMMENT_END)
        }
        this["multi comment"] = "/* comment */" to buildList {
            token("/*", COMMENT_START)
            token(" comment ", COMMENT)
            token("*/", COMMENT_END)
        }
        this["open comment"] = "/* comment" to buildList {
            token("/*", COMMENT_START)
            token(" comment", COMMENT)
        }
        this["nested slash-slash comment"] = "// comment // comment" to buildList {
            token("//", COMMENT_START)
            token(" comment // comment", COMMENT)
        }
        this["nested slash-hash comment"] = "// comment # comment" to buildList {
            token("//", COMMENT_START)
            token(" comment # comment", COMMENT)
        }
        this["nested multi-multi comment"] = "/* comm /* comment */ ent */" to buildList {
            token("/*", COMMENT_START)
            token(" comm /* comment ", COMMENT)
            token("*/", COMMENT_END)
            token(" ent */", CONTENT)
        }
        this["nested slash-multi comment"] = "// comm /* comment */ ent" to buildList {
            token("//", COMMENT_START)
            token(" comm /* comment */ ent", COMMENT)
        }
        this["nested multi-slash comment"] = "/* comm // ent */" to buildList {
            token("/*", COMMENT_START)
            token(" comm // ent ", COMMENT)
            token("*/", COMMENT_END)
        }
        this["quote in comment"] = "// \"cool\" comment" to buildList {
            token("//", COMMENT_START)
            token(" \"cool\" comment", COMMENT)
        }
        this["comment in quote"] = "\"nice // quote\"" to buildList {
            token("\"nice // quote\"", CONTENT)
        }
        this["invalid comment quote"] = "/* comm \"ent */ wtf\"" to buildList {
            token("/*", COMMENT_START)
            token(" comm \"ent ", COMMENT)
            token("*/", COMMENT_END)
            token(" wtf\"", CONTENT)
        }
        this["invalid quote comment"] = "\"quote /* comm\" ent*/" to buildList {
            token("\"quote /* comm\" ent*/", CONTENT)
        }
        this["single quote in doubles"] = "\" quote '\"" to buildList {
            token("\" quote '\"", CONTENT)
        }
        this["double quote in singles"] = "' quote \"'" to buildList {
            token("' quote \"'", CONTENT)
        }
        this["double quote in doc"] = "\"\"\" still \"quote\" \"\"\"" to buildList {
            token("\"\"\" still \"quote\" \"\"\"", CONTENT)
        }
        this["escaped quote"] = "\" quote \\\" \"" to buildList {
            token("\" quote \\\" \"", CONTENT)
        }
    }

    fun check(input: String, expected: List<Token>) =
        assertEquals(expected.asSequence().yaml(), input.scan().toList().dropLast(1).yaml())

    @TestFactory
    fun `test scanner`() =
        tests.map { DynamicTest.dynamicTest(it.key) { check(it.value.first, it.value.second) } }
}