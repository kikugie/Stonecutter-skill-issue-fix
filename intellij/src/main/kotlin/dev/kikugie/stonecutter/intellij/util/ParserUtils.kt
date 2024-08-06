package dev.kikugie.stonecutter.intellij.util

import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.parser.CommentParser

fun String.parse(): Definition? {
    val lexer = Lexer(this)
    val parser = CommentParser(lexer, StoringErrorHandler())
    return parser.parse()
}