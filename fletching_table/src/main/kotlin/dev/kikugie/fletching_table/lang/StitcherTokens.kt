package dev.kikugie.fletching_table.lang

import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.token.StitcherTokenType
import dev.kikugie.stitcher.data.token.TokenType

class StitcherType(name: String) : IElementType(name, StitcherLang)

val CONDITION_MARKER = StitcherType("Condition Marker")
val SWAP_MARKER = StitcherType("Swap Marker")
val SUGAR = StitcherType("Condition Sugar")
val UNARY = StitcherType("Unary Operator")
val BINARY = StitcherType("Binary Operator")
val GROUP_OPEN = StitcherType("Group Open")
val GROUP_CLOSE = StitcherType("Group Close")
val EXPECT_WORD = StitcherType("Expect Word Marker")
val SCOPE_OPEN = StitcherType("Scope Open Marker")
val SCOPE_CLOSE = StitcherType("Scope Close Marker")
val IDENTIFIER = StitcherType("Identifier")
val ASSIGNMENT = StitcherType("Assignment")
val PREDICATE = StitcherType("Predicate")
val UNASSIGNED = StitcherType("Unassigned")
val WHITESPACE = StitcherType("Whitespace")

fun convert(type: TokenType): StitcherType = when (type) {
    MarkerType.CONDITION -> CONDITION_MARKER
    MarkerType.SWAP -> SWAP_MARKER
    StitcherTokenType.SCOPE_OPEN -> SCOPE_OPEN
    StitcherTokenType.SCOPE_CLOSE -> SCOPE_CLOSE
    StitcherTokenType.GROUP_OPEN -> GROUP_OPEN
    StitcherTokenType.GROUP_CLOSE -> GROUP_CLOSE
    StitcherTokenType.NEGATE -> UNARY
    StitcherTokenType.ASSIGN -> ASSIGNMENT
    StitcherTokenType.AND -> BINARY
    StitcherTokenType.OR -> BINARY
    StitcherTokenType.EXPECT_WORD -> EXPECT_WORD
    StitcherTokenType.IDENTIFIER -> IDENTIFIER
    StitcherTokenType.PREDICATE -> PREDICATE
    StitcherTokenType.IF, StitcherTokenType.ELSE, StitcherTokenType.ELIF -> SUGAR
    else -> throw IllegalArgumentException("Inconvertible type $type")
}