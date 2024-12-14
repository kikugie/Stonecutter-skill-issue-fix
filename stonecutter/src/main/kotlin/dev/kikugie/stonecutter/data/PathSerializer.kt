package dev.kikugie.stonecutter.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

/**
 * [KSerializer] implementation for serializing and deserializing [Path]s as strings because there's no default implementation thank you, Kotlin.
 */
object PathSerializer : KSerializer<Path> {
    /** Serializes paths as string primitives.**/
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)
    /** Reads string data from the [decoder] and converts it to a [Path].**/
    override fun deserialize(decoder: Decoder): Path = Path(decoder.decodeString())
    /** Encodes the given path [value] as a string in the [encoder].**/
    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.invariantSeparatorsPathString)
}