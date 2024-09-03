package dev.kikugie.stitcher.lexer

interface LexerAccess {
    val errors: Collection<Pair<LexSlice, String>>

    fun peek(): LexSlice?
    fun lookup(): LexSlice?
    fun advance(): LexSlice?

    fun rawLookup(offset: Int = 0): LexSlice?
    fun rawAdvance(): LexSlice?

    operator fun get(index: Int): LexSlice?
}