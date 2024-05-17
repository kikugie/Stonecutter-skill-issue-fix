package dev.kikugie.stitcher.token

import dev.kikugie.stitcher.type.NULL
import dev.kikugie.stitcher.type.TokenType
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
    val type: TokenType,
) {
    fun take(local: IntRange, type: TokenType) =
        Token(value.substring(local), type)

    companion object {
        val EOF = Token("\u0000", NULL)
    }
}
