package dev.kikugie.stitcher.data

import dev.kikugie.stitcher.type.NULL
import dev.kikugie.stitcher.type.TokenType
import kotlinx.serialization.Serializable

/**
 * Class representing a lexical token in a Stitcher program.
 *
 * @property value The string value of the token.
 * @property type The type of the token.
 */
@Serializable
data class Token(
    val value: String,
    val type: TokenType,
) {
    fun take(local: IntRange, type: TokenType) =
        Token(value.substring(local), type)

    companion object {
        val EOF = Token("\u0000", NULL)
    }

    /**
     * Utility class for representing a found token,
     *
     * @property value The actual value of the token that was matched.
     * @property range The range of indices within the string where the token was found.
     */
    data class Match(
        val value: String,
        val range: IntRange,
    )
}