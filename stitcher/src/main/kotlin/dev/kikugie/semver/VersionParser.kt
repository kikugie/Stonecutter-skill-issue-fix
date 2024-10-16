package dev.kikugie.semver

import dev.kikugie.semver.VersionComparisonOperator.Companion.operatorLength
import dev.kikugie.stitcher.lexer.TokenMatcher.Companion.isValidIdentifier
import dev.kikugie.stitcher.util.StringUtil.countStart

object VersionParser {
    private val INVALID_NUMBER = VersionParsingException("Invalid version number")
    private val INVALID_PRE_RELEASE = VersionParsingException("Invalid pre-release")
    private val INVALID_BUILD_METADATA = VersionParsingException("Invalid build metadata")
    private val EMPTY_VERSION = VersionParsingException("Empty version")
    private val EMPTY_PRE_RELEASE = VersionParsingException("Missing pre-release")
    private val EMPTY_BUILD_METADATA = VersionParsingException("Missing build metadata")
    private val NOT_FULL_MATCH = VersionParsingException("Not a full match")

    data class ParseResult<T>(
        val value: T,
        val end: Int
    )

    @Throws(VersionParsingException::class)
    fun parse(input: CharSequence, start: Int = 0, full: Boolean = false): ParseResult<SemanticVersion> =
        parseSemanticVersion(input, start).also { if (full) it.requireFullMatch(input) }

    @Throws(VersionParsingException::class)
    fun parseLenient(input: CharSequence, start: Int = 0, full: Boolean = false): ParseResult<out Version> = try {
        parseSemanticVersion(input, start)
    } catch (_: VersionParsingException) {
        parseStringVersion(input, start)
    }.also { if (full) it.requireFullMatch(input) }

    @Throws(VersionParsingException::class)
    fun parsePredicate(input: CharSequence, start: Int = 0, full: Boolean = false): ParseResult<VersionPredicate> {
        if (start >= input.length || input[start].isWhitespace()) throw EMPTY_VERSION
        var len = input.operatorLength(start)
        val operator = VersionComparisonOperator.match(input.substring(start, start + len))
        len += input.countStart(start + len, ' ')
        val (version, end) = parse(input, start + len, full)
        return VersionPredicate(operator, version) end end
    }

    @Throws(VersionParsingException::class)
    fun parsePredicateLenient(input: CharSequence, start: Int = 0, full: Boolean = false): ParseResult<VersionPredicate> {
        if (start >= input.length || input[start].isWhitespace()) throw EMPTY_VERSION
        var len = input.operatorLength(start)
        val operator = VersionComparisonOperator.match(input.substring(start, start + len))
        len += input.countStart(start + len, ' ')
        val (version, end) = parseLenient(input, start + len, full)
        return VersionPredicate(operator, version) end end
    }

    private fun ParseResult<*>.requireFullMatch(input: CharSequence) {
        if (end != input.length) throw NOT_FULL_MATCH
    }

    private fun parseSemanticVersion(input: CharSequence, start: Int): ParseResult<SemanticVersion> {
        if (start >= input.length || input[start].isWhitespace()) throw EMPTY_VERSION
        val (components, cursor) = parseComponents(input, start)
        return when (input.getOrNull(cursor)) {
            '-' -> parsePreRelease(input, cursor + 1, components)
            '+' -> parsePostRelease(input, cursor + 1, components)
            else -> SemanticVersion(components) end cursor
        }
    }

    private fun parseComponents(input: CharSequence, start: Int): ParseResult<IntArray> {
        var cursor = start
        var builder = -1
        val components = mutableListOf<Int>()

        while (cursor < input.length) when (val ch = input[cursor]) {
            in '0'..'9' -> {
                builder = if (builder < 0) ch - '0'
                else (builder * 10) + (ch - '0')
                cursor++
            }

            '.' -> {
                if (builder < 0) throw INVALID_NUMBER
                components.add(builder)
                builder = -1
                cursor++
            }

            else -> break
        }
        if (builder < 0) throw INVALID_NUMBER
        components.add(builder)
        return components.toIntArray() end cursor
    }

    private fun parsePreRelease(input: CharSequence, start: Int, components: IntArray): ParseResult<SemanticVersion> {
        var cursor = start
        val builder = StringBuilder()
        while (cursor < input.length) when (val ch = input[cursor]) {
            '+' -> return parsePostRelease(input, cursor + 1, components, builder)
            in 'a'..'z', in 'A'..'Z', in '0'..'9' -> {
                builder.append(ch); cursor++
            }

            '-', '_', '.' -> when (builder.lastOrNull()) {
                null, '.' -> throw INVALID_PRE_RELEASE
                else -> {
                    builder.append(ch); cursor++
                }
            }

            else -> break
        }
        if (builder.isEmpty()) throw EMPTY_PRE_RELEASE
        return SemanticVersion(components, builder.toString()) end cursor
    }

    private fun parsePostRelease(
        input: CharSequence,
        start: Int,
        components: IntArray,
        pre: CharSequence = ""
    ): ParseResult<SemanticVersion> {
        var cursor = start
        val builder = StringBuilder()
        while (cursor < input.length) when (val ch = input[cursor]) {
            in 'a'..'z', in 'A'..'Z', in '0'..'9' -> {
                builder.append(ch); cursor++
            }

            '-', '_', '.' -> when (builder.lastOrNull()) {
                null, '.' -> throw INVALID_BUILD_METADATA
                else -> {
                    builder.append(ch); cursor++
                }
            }

            else -> break
        }
        if (builder.isEmpty()) throw EMPTY_BUILD_METADATA
        return SemanticVersion(components, pre.toString(), builder.toString()) end cursor
    }

    private fun parseStringVersion(input: CharSequence, start: Int): ParseResult<StringVersion> {
        if (start >= input.length || input[start].isWhitespace()) throw EMPTY_VERSION
        var cursor = start
        while (cursor < input.length) if (input[cursor++].isValidIdentifier()) break
        return StringVersion(input.substring(start, cursor)) end cursor
    }

    private infix fun <T> T.end(end: Int) = ParseResult(this, end)
}