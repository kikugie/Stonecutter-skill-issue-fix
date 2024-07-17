package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.LexerAccess

class StitcherLexerAccess(
    private val builder: PsiBuilder,
) : LexerAccess, ErrorHandler {
    override val errors: MutableList<Pair<LexSlice, String>> = mutableListOf()
    override fun accept(token: LexSlice, message: String) {
        errors += token to message
        builder.error(message)
    }

    override fun lookupOrDefault(offset: Int): LexSlice {
        return lookup(offset) ?: LexSlice(NullType, builder.originalText.lastIndex..<builder.originalText.length, builder.originalText)
    }

    override fun lookup(offset: Int): LexSlice? = toSlice(builder.rawLookup(offset), builder.rawTokenTypeStart(offset))

    override fun advance(): LexSlice? {
        builder.advanceLexer()
        return toSlice()
    }

    private fun toSlice(type: IElementType? = builder.tokenType, start: Int = builder.currentOffset): LexSlice? {
        if (type == null) return null
        val conv = convert(type as StitcherType)
        val range = start..<start + if (type == builder.tokenType) builder.tokenText!!.length else 0
        return LexSlice(conv, range, builder.originalText)
    }
}