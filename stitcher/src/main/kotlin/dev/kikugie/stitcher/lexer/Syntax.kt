package dev.kikugie.stitcher.lexer

import dev.kikugie.stitcher.data.token.StitcherTokenType
import dev.kikugie.stitcher.data.token.WhitespaceType

internal val WHITESPACE = WhitespaceRecognizer(WhitespaceType)

internal val SCOPE_OPEN = CharRecognizer('{', StitcherTokenType.SCOPE_OPEN)
internal val SCOPE_CLOSE = CharRecognizer('}', StitcherTokenType.SCOPE_CLOSE)
internal val GROUP_OPEN = CharRecognizer('(', StitcherTokenType.GROUP_OPEN)
internal val GROUP_CLOSE = CharRecognizer(')', StitcherTokenType.GROUP_CLOSE)
internal val ASSIGN = CharRecognizer(':', StitcherTokenType.ASSIGN)
internal val NEGATE = CharRecognizer('!', StitcherTokenType.NEGATE)

internal val EXPECT_WORD = StringRecognizer(">>", StitcherTokenType.EXPECT_WORD)
internal val AND = StringRecognizer("&&", StitcherTokenType.AND)
internal val OR = StringRecognizer("||", StitcherTokenType.OR)
internal val IF = StringRecognizer("if", StitcherTokenType.IF)
internal val ELSE = StringRecognizer("else", StitcherTokenType.ELSE)
internal val ELIF = StringRecognizer("elif", StitcherTokenType.ELIF)
internal val IDENTIFIER = IdentifierRecognizer(StitcherTokenType.IDENTIFIER)
internal val PREDICATE = PredicateRecognizer(StitcherTokenType.PREDICATE)

internal val ALL = listOf(
    WHITESPACE,
    SCOPE_OPEN,
    SCOPE_CLOSE,
    GROUP_OPEN,
    GROUP_CLOSE,
    ASSIGN,
    NEGATE,
    EXPECT_WORD,
    AND,
    OR,
    IF,
    ELSE,
    ELIF,
    PREDICATE,
    IDENTIFIER,
)