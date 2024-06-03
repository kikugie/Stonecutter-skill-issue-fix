package dev.kikugie.stitcher.data.token

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import dev.kikugie.stitcher.data.component.Component

/**
 * A lexical token in a Stitcher program.
 *
 * @property value literal value of the token
 * @property type associated token type
 * @see [TokenType]
 */
@Serializable
data class Token(
    val value: String,
    val type: TokenType,
) {
    /**
     * Metadata associated with a variable.
     * This is stored for error reporting and to avoid double-parsing some values.
     *
     * Deserialized tokens are assumed to be valid and shouldn't need this.
     * @property metadata Map containing the metadata
     */
    private val metadata: MutableMap<KClass<*>, Any> by lazy { mutableMapOf() }

    fun isBlank() = value.isBlank()

    /**
     * Copies this token, changing the type.
     * This is done by the parser in case lexer has identified the type incorrectly.
     * This method is preferable to manual instance creation, because it preserves [metadata]
     *
     * @param type New type
     */
    fun withType(type: TokenType) = Token(value, type).also {
        it.metadata.putAll(metadata)
    }

    operator fun <T : Any> set(key: KClass<T>, value: T) {
        metadata[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: KClass<T>): T? = metadata[key]?.let { it as? T }
    inline fun <reified T : Any> get(): T? = get(T::class)

    companion object {
        /**
         * A token with no value. This is usually used as a placeholder in [Component]s by the parser.
         */
        val EMPTY = Token("", NullType)
    }

    data class Match(
        val value: String,
        val range: IntRange,
    )
}