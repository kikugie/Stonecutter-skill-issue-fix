package dev.kikugie.stitcher.lexer

@Suppress("MemberVisibilityCanBePrivate")
object DefaultRecognizers {
//    val CONDITION = CharRecognizer('?')
//    val SWAP = CharRecognizer('$')

    val BLOCK_OPEN = CharRecognizer('{')
    val BLOCK_CLOSE = CharRecognizer('}')

    val AND = StringRecognizer("&&")
    val OR = StringRecognizer("||")
    val IF = StringRecognizer("if")
    val ELSE = StringRecognizer("else")
    val ELIF = StringRecognizer("elif")

    val conditionState = listOf(
        StitcherTokenType.IF to IF,
        StitcherTokenType.ELIF to ELIF,
        StitcherTokenType.ELSE to ELSE,
        StitcherTokenType.AND to AND,
        StitcherTokenType.OR to OR,

        StitcherTokenType.BLOCK_OPEN to BLOCK_OPEN,
        StitcherTokenType.BLOCK_CLOSE to BLOCK_CLOSE,
    )

    val swapState = listOf(
        StitcherTokenType.BLOCK_OPEN to BLOCK_OPEN,
        StitcherTokenType.BLOCK_CLOSE to BLOCK_CLOSE,
    )
}