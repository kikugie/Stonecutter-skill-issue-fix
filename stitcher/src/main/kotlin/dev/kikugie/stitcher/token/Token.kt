package dev.kikugie.stitcher.token

import dev.kikugie.stitcher.util.IntRangeSerializer
import dev.kikugie.stitcher.util.shift
import kotlinx.serialization.Serializable

/**
 * Class representing a lexical token in a Stitcher program.
 *
 * @property value The string value of the token.
 * @property range The range of indices in the original source code where the token appears.
 * @property type The type of the token.
 */
@Serializable
data class Token(
    val value: String,
    val range: @Serializable(with = IntRangeSerializer::class) IntRange,
    val type: TokenType,
) {
    fun subtoken(local: IntRange, type: TokenType) =
        Token(value.substring(local), local.shift(range), type)

    companion object {
        fun eof(position: Int) = Token("\u0000", position..-1, NULL)
    }
}
