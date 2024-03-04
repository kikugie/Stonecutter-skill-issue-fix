package dev.kikugie.stitcher.token

/**
 * Utility class for representing a found token,
 *
 * @property value The actual value of the token that was matched.
 * @property range The range of indices within the string where the token was found.
 */
data class TokenMatch(
    val value: String,
    val range: IntRange,
)
