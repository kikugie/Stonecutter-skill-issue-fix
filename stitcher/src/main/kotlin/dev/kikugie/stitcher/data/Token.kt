package dev.kikugie.stitcher.data

import kotlinx.serialization.Serializable

/**
 * Class representing a lexical id in a Stitcher program.
 *
 * @property value The string value of the id.
 * @property type The id of the id.
 */
@Serializable
data class Token(
    val value: String,
    val type: TokenType,
) {
    fun isBlank() = this === EMPTY || value.isBlank()

    fun take(local: IntRange, type: TokenType) =
        Token(value.substring(local), type)

    companion object {
        val EMPTY = Token("\u0000", NullType)
    }

    /**
     * Utility class for representing a found id,
     *
     * @property value The actual value of the id that was matched.
     * @property range The range of indices within the string where the id was found.
     */
    data class Match(
        val value: String,
        val range: IntRange,
    )
}