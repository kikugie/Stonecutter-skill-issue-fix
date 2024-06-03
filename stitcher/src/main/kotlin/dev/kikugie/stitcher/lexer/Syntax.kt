package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.StitcherTokenType

private val SCOPE_OPEN = CharRecognizer('{', StitcherTokenType.SCOPE_OPEN)
private val SCOPE_CLOSE = CharRecognizer('}', StitcherTokenType.SCOPE_CLOSE)
private val EXPECT_WORD = StringRecognizer(">>", StitcherTokenType.EXPECT_WORD)

private val GROUP_OPEN = CharRecognizer('(', StitcherTokenType.GROUP_OPEN)
private val GROUP_CLOSE = CharRecognizer(')', StitcherTokenType.GROUP_CLOSE)

private val ASSIGN = CharRecognizer(':', StitcherTokenType.ASSIGN)
private val NEGATE = CharRecognizer('!', StitcherTokenType.NEGATE)
private val AND = StringRecognizer("&&", StitcherTokenType.AND)
private val OR = StringRecognizer("||", StitcherTokenType.OR)
private val IF = StringRecognizer("if", StitcherTokenType.IF)
private val ELSE = StringRecognizer("else", StitcherTokenType.ELSE)
private val ELIF = StringRecognizer("elif", StitcherTokenType.ELIF)
private val IDENTIFIER = IdentifierRecognizer(StitcherTokenType.IDENTIFIER)
private val PREDICATE = PredicateRecognizer(StitcherTokenType.PREDICATE)

internal val ALL: List<TokenRecognizer<StitcherTokenType>> = listOf(
    GROUP_OPEN,
    GROUP_CLOSE,
    SCOPE_OPEN,
    SCOPE_CLOSE,
    ASSIGN,
    NEGATE,
    IF,
    ELSE,
    ELIF,
    AND,
    OR,
    EXPECT_WORD,
    IDENTIFIER,
    PREDICATE
)
