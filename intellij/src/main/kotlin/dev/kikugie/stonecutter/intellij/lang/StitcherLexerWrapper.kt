package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.lexer.Lexer

class StitcherLexerWrapper : LexerBase() {
    private lateinit var sequence: CharSequence
    private lateinit var lexer: Lexer
    private val current get() = lexer.lookup()

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        sequence = buffer
        lexer = Lexer(buffer)
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = current?.type?.let(::convert)

    override fun getTokenStart(): Int = current?.range?.first ?: sequence.length

    override fun getTokenEnd(): Int = current?.let { it.range.last + 1 } ?: sequence.length

    override fun getBufferSequence(): CharSequence = sequence

    override fun getBufferEnd(): Int = sequence.length

    override fun advance() {
        lexer.advance()
    }
}