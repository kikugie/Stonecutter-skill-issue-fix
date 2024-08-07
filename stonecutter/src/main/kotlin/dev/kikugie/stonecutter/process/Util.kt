@file:OptIn(ExperimentalSerializationApi::class)

package dev.kikugie.stonecutter.process

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stonecutter.configuration.StonecutterData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

val YAML = Yaml(
    configuration = YamlConfiguration(
        strictMode = false
    )
)


inline fun <reified T> Path.decode(): T = Cbor.Default.decodeFromByteArray(readBytes())
inline fun <reified T> Path.encode(value: T) {
    parent.createDirectories()
    writeBytes(
        Cbor.Default.encodeToByteArray(value),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
    )
}

inline fun <reified T> Path.decodeYaml(): T = inputStream().use { YAML.decodeFromStream(it) }
inline fun <reified T> Path.encodeYaml(value: T) {
    parent.createDirectories()
    outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
        YAML.encodeToStream(value, it)
    }
}

inline fun <T> runIgnoring(action: () -> T): T? = try {
    action()
} catch (_: Exception) {
    null
}

fun StonecutterData.toParams(version: String, key: String = "minecraft"): TransformParameters {
    val deps = dependencies.toMutableMap()
    val dest = deps[key] ?: VersionParser.parseLenient(version)
    deps[key] = dest
    deps[""] = dest
    return TransformParameters(swaps, constants, deps)
}