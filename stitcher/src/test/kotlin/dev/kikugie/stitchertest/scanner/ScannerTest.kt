package dev.kikugie.stitchertest.scanner

import dev.kikugie.stitcher.scanner.CommentType.*
import dev.kikugie.stitcher.scanner.HashSingleLine
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitchertest.token
import kotlin.test.Test

class ScannerTest {
    companion object {
        val recognizers = listOf(StandardSingleLine, StandardMultiLine, HashSingleLine)
    }

    @Test
    fun `slash comment`() {
        val expected = buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment", 2..9, COMMENT)
        }
        check(Samples.`slash comment`, expected)
    }

    @Test
    fun `hash comment`() {
        val expected = buildList {
            token("#", 0..0, COMMENT_START)
            token(" comment", 1..8, COMMENT)
        }
        check(Samples.`hash comment`, expected)
    }

    @Test
    fun `multi comment`() {
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comment ", 2..10, COMMENT)
            token("*/", 11..12, COMMENT_END)
        }
        check(Samples.`multi comment`, expected)
    }

    @Test
    fun `doc comment`() {
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token("*\n * doc comment\n ", 2..19, COMMENT)
            token("*/", 20..21, COMMENT_END)
        }
        check(Samples.`doc comment`, expected)
    }

    @Test
    fun `open comment`() {
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comment", 2..9, COMMENT)
        }
        check(Samples.`open comment`, expected)
    }

    @Test
    fun `nested slash-slash comment`() {
        val expected = buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment // comment", 2..20, COMMENT)
        }
        check(Samples.`nested slash-slash comment`, expected)
    }

    @Test
    fun `nested slash-hash comment`() {
        val expected = buildList {
            token("//", 0..1, COMMENT_START)
            token(" comment # comment", 2..19, COMMENT)
        }
        check(Samples.`nested slash-hash comment`, expected)
    }

    @Test
    fun `nested multi-multi comment`() {
        // Nested comments should be avoided because of language specific differences
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm /* comment ", 2..18, COMMENT)
            token("*/", 19..20, COMMENT_END)
            token(" ent */", 21..27, CONTENT)
        }
        check(Samples.`nested multi-multi comment`, expected)
    }

    @Test
    fun `nested slash-multi comment`() {
        val expected = buildList {
            token("//", 0..1, COMMENT_START)
            token(" comm /* comment */ ent", 2..24, COMMENT)
        }
        check(Samples.`nested slash-multi comment`, expected)
    }

    @Test
    fun `nested multi-slash comment`() {
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm // ent ", 2..14, COMMENT)
            token("*/", 15..16, COMMENT_END)
        }
        check(Samples.`nested multi-slash comment`, expected)
    }

    @Test
    fun `quotes in comment`() {
        val expected = buildList {
            token("//", 0..1, COMMENT_START)
            token(" \"cool\" comment", 2..16, COMMENT)
        }
        check(Samples.`quotes in comment`, expected)
    }

    @Test
    fun `comment in quotes`() {
        val expected = buildList {
            token("\"nice // quote\"", 0..14, CONTENT)
        }
        check(Samples.`comment in quotes`, expected)
    }

    @Test
    fun `invalid comment quote`() {
        val expected = buildList {
            token("/*", 0..1, COMMENT_START)
            token(" comm \"ent ", 2..12, COMMENT)
            token("*/", 13..14, COMMENT_END)
            token(" wtf\"", 15..19, CONTENT)
        }
        check(Samples.`invalid comment quote`, expected)
    }

    @Test
    fun `invalid quote comment`() {
        val expected = buildList {
            token("\"quote /* comm\" ent*/", 0..20, CONTENT)
        }
        check(Samples.`invalid quote comment`, expected)
    }

    @Test
    fun `single quote in doubles`() {
        val expected = buildList {
            token("\" quote '\"", 0..9, CONTENT)
        }
        check(Samples.`single quote in doubles`, expected)
    }

    @Test
    fun `double quote in singles`() {
        val expected = buildList {
            token("' quote \"'", 0..9, CONTENT)
        }
        check(Samples.`double quote in singles`, expected)
    }

    @Test
    fun `double quote in doc`() {
        val expected = buildList {
            token("\"\"\" still \"quote\" \"\"\"", 0..20, CONTENT)
        }
        check(Samples.`double quote in doc`, expected)
    }

    @Test
    fun `escaped quote`() {
        val expected = buildList {
            token("\" quote \\\" \"", 0..11, CONTENT)
        }
        check(Samples.`escaped quote`, expected)
    }
}