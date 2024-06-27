package dev.kikugie.stitcher.lexer

interface LexerAccess {
    fun lookupOrDefault(offset: Int = 0): Lexer.Slice
    fun lookup(offset: Int = 0): Lexer.Slice?
    fun advance(): Lexer.Slice?
}