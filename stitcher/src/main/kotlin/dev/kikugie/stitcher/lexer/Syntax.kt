package dev.kikugie.stitcher.lexer

@Suppress("MemberVisibilityCanBePrivate")
object Syntax {
//    val CONDITION = CharRecognizer('?')
//    val SWAP = CharRecognizer('$')

    val SCOPE_OPEN = CharRecognizer('{')
    val SCOPE_CLOSE = CharRecognizer('}')

    val GROUP_OPEN = CharRecognizer('(')
    val GROUP_CLOSE = CharRecognizer(')')

    val NEGATE = CharRecognizer('!')
    val AND = StringRecognizer("&&")
    val OR = StringRecognizer("||")
    val IF = StringRecognizer("if")
    val ELSE = StringRecognizer("else")

    val conditionState = listOf(
        StitcherTokenType.IF to IF,
        StitcherTokenType.ELSE to ELSE,
        StitcherTokenType.AND to AND,
        StitcherTokenType.OR to OR,
        StitcherTokenType.NEGATE to NEGATE,

        StitcherTokenType.SCOPE_OPEN to SCOPE_OPEN,
        StitcherTokenType.SCOPE_CLOSE to SCOPE_CLOSE,

        StitcherTokenType.GROUP_OPEN to GROUP_OPEN,
        StitcherTokenType.GROUP_CLOSE to GROUP_CLOSE,
    )

    val swapState = listOf(
        StitcherTokenType.SCOPE_OPEN to SCOPE_OPEN,
        StitcherTokenType.SCOPE_CLOSE to SCOPE_CLOSE,
    )
}