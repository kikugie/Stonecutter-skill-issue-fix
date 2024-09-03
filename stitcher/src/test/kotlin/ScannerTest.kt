import dev.kikugie.stitcher.data.token.ContentType.*
import dev.kikugie.stitcher.data.token.Token
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object ScannerTest {
    val TESTS = buildList {
        add("slash comment", "// comment") {
            token("//", COMMENT_START)
            token(" comment", COMMENT)
        }
        add("hash comment", "# comment") {
            token("#", COMMENT_START)
            token(" comment", COMMENT)
        }
        add("doc comment",
            """
            /**
            * doc comment
            */
            """.trimIndent()
        ) {
            token("/*", COMMENT_START)
            token("*\n* doc comment\n", COMMENT)
            token("*/", COMMENT_END)
        }
        add("multi comment", "/* comment */") {
            token("/*", COMMENT_START)
            token(" comment ", COMMENT)
            token("*/", COMMENT_END)
        }
        add("open comment", "/* comment") {
            token("/*", COMMENT_START)
            token(" comment", COMMENT)
        }
        add("nested slash-slash comment", "// comment // comment") {
            token("//", COMMENT_START)
            token(" comment // comment", COMMENT)
        }
        add("nested slash-hash comment", "// comment # comment") {
            token("//", COMMENT_START)
            token(" comment # comment", COMMENT)
        }
        add("nested multi-multi comment", "/* comm /* comment */ ent */") {
            token("/*", COMMENT_START)
            token(" comm /* comment ", COMMENT)
            token("*/", COMMENT_END)
            token(" ent */", CONTENT)
        }
        add("nested slash-multi comment", "// comm /* comment */ ent") {
            token("//", COMMENT_START)
            token(" comm /* comment */ ent", COMMENT)
        }
        add("nested multi-slash comment", "/* comm // ent */") {
            token("/*", COMMENT_START)
            token(" comm // ent ", COMMENT)
            token("*/", COMMENT_END)
        }
        add("quote in comment", "// \"cool\" comment") {
            token("//", COMMENT_START)
            token(" \"cool\" comment", COMMENT)
        }
        add("comment in quote", "\"nice // quote\"") {
            token("\"nice // quote\"", CONTENT)
        }
        add("invalid comment quote", "/* comm \"ent */ wtf\"") {
            token("/*", COMMENT_START)
            token(" comm \"ent ", COMMENT)
            token("*/", COMMENT_END)
            token(" wtf\"", CONTENT)
        }
        add("invalid quote comment", "\"quote /* comm\" ent*/") {
            token("\"quote /* comm\" ent*/", CONTENT)
        }
        add("single quote in doubles", "\" quote '\"") {
            token("\" quote '\"", CONTENT)
        }
        add("double quote in singles", "' quote \"'") {
            token("' quote \"'", CONTENT)
        }
        add("double quote in doc", "\"\"\" still \"quote\" \"\"\"") {
            token("\"\"\" still \"quote\" \"\"\"", CONTENT)
        }
        add("escaped quote", "\" quote \\\" \"") {
            token("\" quote \\\" \"", CONTENT)
        }
    }

    @TestFactory
    fun `test scanner`() = TESTS.map {
        DynamicTest.dynamicTest(it.first) {check(it.second, it.third)}
    }

    private fun check(input: String, expected: List<Token>) = Assertions.assertEquals(
        expected.yaml(),
        input.scan().toList().dropLast(1).yaml()
    )

    private inline fun MutableList<Triple<String, String, List<Token>>>.add(name: String, value: String, action: MutableList<Token>.() -> Unit) {
        add(Triple(name, value, buildList(action)))
    }
}