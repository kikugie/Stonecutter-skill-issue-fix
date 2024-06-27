package dev.kikugie.fletching_table.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.lexer.Lexer.Slice
import dev.kikugie.stitcher.lexer.LexerAccess

class StitcherLexerAccess(
    private val builder: PsiBuilder,
) : LexerAccess, ErrorHandler {
    override val errors: MutableList<Pair<Slice, String>> = mutableListOf()
    override fun accept(token: Slice, message: String) {
        errors += token to message
        builder.error(message)
    }

    override fun lookupOrDefault(offset: Int): Slice {
        return lookup(offset) ?: Slice(NullType, builder.originalText.lastIndex..<builder.originalText.length, builder.originalText)
    }

    override fun lookup(offset: Int): Slice? = toSlice(builder.rawLookup(offset), builder.rawTokenTypeStart(offset))

    override fun advance(): Slice? {
        builder.advanceLexer()
        return toSlice()
    }

    private fun toSlice(type: IElementType? = builder.tokenType, start: Int = builder.currentOffset): Slice? {
        if (type == null) return null
        val conv = convert(type as StitcherType)
        val range = start..<start + if (type == builder.tokenType) builder.tokenText!!.length else 0
        return Slice(conv, range, builder.originalText)
    }
}