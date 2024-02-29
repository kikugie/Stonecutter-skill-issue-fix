package dev.kikugie.stitchertest.scanner

import com.charleskorn.kaml.Yaml
import com.github.ajalt.mordant.rendering.TextColors
import dev.kikugie.stitcher.scanner.CommentType
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.token.Token
import kotlinx.serialization.encodeToString
import kotlin.test.assertEquals

fun printCol(str: CharSequence) = str.split('\n').forEach { println(TextColors.cyan(it)) }
fun String.tokenize() = Scanner(this.reader(), ScannerTest.recognizers).tokenize()
fun Sequence<Token>.yaml() = Yaml.default.encodeToString(this.toList())
fun MutableList<Token>.token(value: String, range: IntRange, type: CommentType) {
    add(Token(value, range, type))
}

fun Sequence<Token>.printSetup() =
    forEach { printCol("token(\"${it.value.replace("\n", "\\n").replace("\"", "\\\"")}\", ${it.range}, ${it.type})") }


fun check(input: String, expected: List<Token>) =
    assertEquals(input.tokenize().yaml().also { printCol(it) }, expected.asSequence().yaml())