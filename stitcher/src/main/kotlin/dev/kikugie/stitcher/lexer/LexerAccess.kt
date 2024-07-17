package dev.kikugie.stitcher.lexer

interface LexerAccess {
    fun lookupOrDefault(offset: Int = 0): LexSlice
    fun lookup(offset: Int = 0): LexSlice?
    fun advance(): LexSlice?
}