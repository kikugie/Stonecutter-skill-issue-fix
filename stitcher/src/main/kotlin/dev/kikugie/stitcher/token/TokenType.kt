package dev.kikugie.stitcher.token

import dev.kikugie.stitcher.util.StringifySerializer
import kotlinx.serialization.Serializable

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = StringifySerializer::class)
interface TokenType

@Serializable
data object NULL : TokenType