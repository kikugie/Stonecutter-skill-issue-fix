package dev.kikugie.stitchertest.util

import dev.kikugie.stitcher.process.Lexer
import dev.kikugie.stitcher.process.Parser
import dev.kikugie.stitcher.process.recognizer.HashSingleLine
import dev.kikugie.stitcher.process.Scanner
import dev.kikugie.stitcher.process.recognizer.StandardMultiLine
import dev.kikugie.stitcher.process.recognizer.StandardSingleLine
import dev.kikugie.stitcher.data.Token

val recognizers = listOf(StandardSingleLine, StandardMultiLine, HashSingleLine)

fun String.scan() = Scanner(this.reader(), recognizers).tokenize()

fun Sequence<Token>.tokenize() = Lexer(this.asIterable()).tokenize()
fun String.tokenize() = scan().tokenize()

fun Sequence<Token>.parse() = Parser(asIterable()).parse()
fun String.parse() = tokenize().parse()