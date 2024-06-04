package dev.kikugie.fletching_table.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.lexer.Lexer

class StitcherLexerWrapper : LexerBase() {
    private lateinit var sequence: CharSequence
    private lateinit var lexer: Lexer

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        sequence = buffer
        lexer = Lexer(buffer)
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = lexer.lookup()?.type?.let(::convert)

    override fun getTokenStart(): Int {
        TODO("Not yet implemented")
    }

    override fun getTokenEnd(): Int {
        TODO("Not yet implemented")
    }

    override fun advance() {
        TODO("Not yet implemented")
    }

    override fun getBufferSequence(): CharSequence {
        TODO("Not yet implemented")
    }

    override fun getBufferEnd(): Int {
        TODO("Not yet implemented")
    }
}