package dev.kikugie.stitcher.data

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Class representing a lexical id in a Stitcher program.
 *
 * @property value The string value of the type.
 * @property type The id of the token.
 */
@Serializable
data class Token(
    val value: String,
    val type: TokenType,
) {
    /**
     * Represents metadata associated with a variable.
     * This is stored for error reporting and to avoid double-parsing some values.
     *
     * Deserialized tokens are assumed to be valid and shouldn't need this.
     * @property metadata The map containing the metadata.
     */
    private val metadata: MutableMap<KClass<*>, Any> by lazy { mutableMapOf() }

    fun isBlank() = this === EMPTY || value.isBlank()

    operator fun <T : Any> set(key: KClass<T>, value: T) {
        metadata[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: KClass<T>): T? = metadata[key]?.let { it as? T }

    companion object {
        val EMPTY = Token("\u0000", NullType)
    }

    data class Match(
        val value: String,
        val range: IntRange,
    )
}