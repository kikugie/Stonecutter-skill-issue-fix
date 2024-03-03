package dev.kikugie.stitcher.token

import dev.kikugie.stitcher.util.IntRangeSerializer
import dev.kikugie.stitcher.util.shift
import kotlinx.serialization.Serializable

@Serializable
open class Token(
    val value: String,
    val range: @Serializable(with = IntRangeSerializer::class) IntRange,
    val type: TokenType,
) {
    override fun toString() = "Token(value=$value, range=$range, type=$type)"

    fun subtoken(local: IntRange, type: TokenType) =
        Token(value.substring(local), local.shift(range), type)

    companion object {
        fun eof(position: Int) = Token("\u0000", position..-1, EOF)
    }
}
