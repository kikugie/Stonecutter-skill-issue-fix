package dev.kikugie.stitcher.type

import dev.kikugie.stitcher.data.Token
import kotlinx.serialization.Serializable

/**
 * Represents a token type in a Stitcher program.
 *
 * This interface is used to define different types of lexical tokens that can be encountered in a Stitcher program.
 * Each token type should implement this interface.
 *
 * @suppress Suppresses the warning for incompatible serializer types.
 * This is not an issue anyway, since all implementations are enums.
 * @see Token
 */
@Serializable
sealed interface TokenType

