package dev.kikugie.stitcher.lexer

import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.data.token.TokenType
import dev.kikugie.stitcher.data.token.WhitespaceType
import dev.kikugie.stitcher.util.StringUtil.countStart

class TokenMatcher(private val input: CharSequence) {
    companion object {
        fun Char.isValidIdentifier() = when (this) {
            in 'a'..'z', in 'A'..'Z', in '0'..'9', '_', '-' -> true
            else -> false
        }
    }

    fun match(offset: Int): LexSlice = matchInternal(offset, input[offset])
        ?: slice(offset..<input.length, NullType)

    private fun matchInternal(offset: Int, ch: Char): LexSlice? = when (ch) {
        '<', '=', '~', '^' -> matchPredicate(offset)
        ' ', '\t' -> matchWhitespace(offset)
        '(' -> slice(offset, GROUP_OPEN)
        ')' -> slice(offset, GROUP_CLOSE)
        '{' -> slice(offset, SCOPE_OPEN)
        '}' -> slice(offset, SCOPE_CLOSE)
        ':' -> slice(offset, ASSIGN)
        '!' -> slice(offset, NEGATE)
        '&' -> matchString(offset, "&&", AND)
        '|' -> matchString(offset, "||", OR)
        'i' -> matchString(offset, "if", IF)
            ?: matchIdentifier(offset)

        'e' -> matchString(offset, "else", ELSE)
            ?: matchString(offset, "elif", ELIF)
            ?: matchIdentifier(offset)

        '>' -> matchString(offset, ">>", EXPECT_WORD)
            ?: matchPredicate(offset)
        else -> matchAny(offset, ch)
    }

    private fun matchWhitespace(offset: Int): LexSlice =
        slice(offset..<offset + input.countStart(offset + 1, ' ', '\t') + 1, WhitespaceType)

    private fun matchString(offset: Int, pattern: String, type: TokenType): LexSlice? = when {
        offset + pattern.length > input.length -> null
        input.substring(offset, offset + pattern.length) != pattern -> null
        else -> slice(offset..<offset + pattern.length, type)
    }

    private fun matchAny(offset: Int, ch: Char): LexSlice? = when (ch) {
        in '0'..'9' -> matchPredicate(offset) ?: matchIdentifier(offset)
        in 'a'..'z', in 'A'..'Z', '_', '-' -> matchIdentifier(offset)
        else -> null
    }

    private fun matchIdentifier(offset: Int): LexSlice {
        val count = input.countStart(offset + 1) { it.isValidIdentifier() } + 1
        return slice(offset..<offset + count, IDENTIFIER)
    }

    private fun matchPredicate(offset: Int): LexSlice? = try {
        val end = VersionParser.parsePredicate(input, offset).end
        slice(offset..<end, PREDICATE)
    } catch (e: VersionParsingException) {
        null
    }

    private fun slice(range: IntRange, type: TokenType) =
        LexSlice(type, range, input)

    private fun slice(index: Int, type: TokenType) =
        LexSlice(type, index..index, input)
}