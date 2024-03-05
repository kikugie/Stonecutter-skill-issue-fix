package dev.kikugie.stitchertest.util

import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.parser.Parser
import dev.kikugie.stitcher.scanner.HashSingleLine
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitcher.token.Token

val recognizers = listOf(StandardSingleLine, StandardMultiLine, HashSingleLine)

fun String.scan() = Scanner(this.reader(), recognizers).tokenize()

fun Sequence<Token>.tokenize() = Lexer(this.asIterable()).tokenize()
fun String.tokenize() = scan().tokenize()

fun Sequence<Token>.parse() = Parser(asIterable()).parse()
fun String.parse() = tokenize().parse()