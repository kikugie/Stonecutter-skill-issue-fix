@file:OptIn(ExperimentalSerializationApi::class)

package dev.kikugie.stonecutter.process

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

inline fun <reified T> Path.decode(): T = Cbor.Default.decodeFromByteArray(readBytes())
inline fun <reified T> Path.encode(value: T) = writeBytes(
    Cbor.Default.encodeToByteArray(value),
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING
)

inline fun <T> runIgnoring(action: () -> T): T? = try {
    action()
} catch (_: Exception) {
    null
}