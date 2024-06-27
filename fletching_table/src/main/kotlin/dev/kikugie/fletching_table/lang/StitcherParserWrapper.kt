package dev.kikugie.fletching_table.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.parser.CommentParser

class StitcherParserWrapper : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val wrapper = StitcherLexerAccess(builder)
        val parser = CommentParser(wrapper, wrapper)
        val mark = builder.mark()
        parser.parse()
        mark.done(root)
        return builder.treeBuilt
    }
}