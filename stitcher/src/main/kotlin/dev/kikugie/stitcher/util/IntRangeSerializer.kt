package dev.kikugie.stitcher.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): IntRange =
        decoder.decodeString().split("..<", limit = 2).let { it[0].toInt()..<it[1].toInt() }

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeString("${value.first}..<${value.last + 1}")
    }
}