package dev.kikugie.stonecutter.intellij.lang

import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.*

class StitcherType(name: String) : IElementType(name, StitcherLang)

val CONDITION_MARKER = StitcherType("Condition Marker")
val SWAP_MARKER = StitcherType("Swap Marker")
val IF = StitcherType("If")
val ELSE = StitcherType("Else")
val ELIF = StitcherType("Elif")
val NEGATE = StitcherType("Negation Operator")
val AND = StitcherType("And Operator")
val OR = StitcherType("Or Operator")
val GROUP_OPEN = StitcherType("Group Open")
val GROUP_CLOSE = StitcherType("Group Close")
val EXPECT_WORD = StitcherType("Expect Word Marker")
val SCOPE_OPEN = StitcherType("Scope Open Marker")
val SCOPE_CLOSE = StitcherType("Scope Close Marker")
val IDENTIFIER = StitcherType("Identifier")
val ASSIGNMENT = StitcherType("Assignment")
val PREDICATE = StitcherType("Predicate")
val WHITESPACE = StitcherType("Whitespace")
val UNKNOWN = StitcherType("Unknown")

fun convert(type: TokenType): StitcherType = when (type) {
    WhitespaceType -> WHITESPACE
    NullType -> UNKNOWN
    MarkerType.CONDITION -> CONDITION_MARKER
    MarkerType.SWAP -> SWAP_MARKER
    StitcherTokenType.SCOPE_OPEN -> SCOPE_OPEN
    StitcherTokenType.SCOPE_CLOSE -> SCOPE_CLOSE
    StitcherTokenType.GROUP_OPEN -> GROUP_OPEN
    StitcherTokenType.GROUP_CLOSE -> GROUP_CLOSE
    StitcherTokenType.NEGATE -> NEGATE
    StitcherTokenType.ASSIGN -> ASSIGNMENT
    StitcherTokenType.AND -> AND
    StitcherTokenType.OR -> OR
    StitcherTokenType.EXPECT_WORD -> EXPECT_WORD
    StitcherTokenType.IDENTIFIER -> IDENTIFIER
    StitcherTokenType.PREDICATE -> PREDICATE
    StitcherTokenType.IF -> IF
    StitcherTokenType.ELSE -> ELSE
    StitcherTokenType.ELIF -> ELIF
    else -> UNKNOWN
}

fun convert(type: StitcherType): TokenType = when (type) {
    UNKNOWN -> NullType
    WHITESPACE -> WhitespaceType
    CONDITION_MARKER -> MarkerType.CONDITION
    SWAP_MARKER -> MarkerType.SWAP
    SCOPE_OPEN -> StitcherTokenType.SCOPE_OPEN
    SCOPE_CLOSE -> StitcherTokenType.SCOPE_CLOSE
    GROUP_OPEN -> StitcherTokenType.GROUP_OPEN
    GROUP_CLOSE -> StitcherTokenType.GROUP_CLOSE
    ASSIGNMENT -> StitcherTokenType.ASSIGN
    EXPECT_WORD -> StitcherTokenType.EXPECT_WORD
    IDENTIFIER -> StitcherTokenType.IDENTIFIER
    PREDICATE -> StitcherTokenType.PREDICATE
    IF -> StitcherTokenType.IF
    ELSE -> StitcherTokenType.ELSE
    ELIF -> StitcherTokenType.ELIF
    NEGATE -> StitcherTokenType.NEGATE
    AND -> StitcherTokenType.AND
    OR -> StitcherTokenType.OR
    else -> NullType
}