package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.scanner.HashSingleLine
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitcher.data.token.TokenType
import org.intellij.lang.annotations.Language


val recognizers = listOf(StandardSingleLine, StandardMultiLine, HashSingleLine)

fun String.scan() = Scanner(this.reader(), recognizers).tokenize()
//
//fun Sequence<Token>.tokenize() = Lexer(this.asIterable()).tokenize()
//fun String.tokenize() = scan().tokenize()
//
//fun Sequence<Token>.parse() = FileParser(asIterable()).parse()
//fun String.parse() = tokenize().parse()

fun MutableList<Token>.token(value: String, type: TokenType) {
    add(Token(value, type))
}

data class Tuple<A, B, C>(val left: A, val middle: B, val right: C)

inline fun <A, B, C> MutableCollection<Tuple<A, B, List<C>>>.add(left: A, middle: B, right: MutableList<C>.() -> Unit) {
    add(Tuple(left, middle, mutableListOf<C>().apply(right)))
}

fun <A, B, C> MutableCollection<Tuple<A, B, C>>.tuple(left: A, middle: B, @Language("yaml") right: C) {
    add(Tuple(left, middle, right))
}