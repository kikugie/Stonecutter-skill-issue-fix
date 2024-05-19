package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.RootScope
import dev.kikugie.stitcher.data.Scope
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Parser.Companion.parse
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.Transformer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.*
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.measureTime

object Time {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class,
        ExperimentalPathApi::class
    )
    @Test
    fun test() {
    }

    fun Path.hash(algorithm: String): String {
        return MessageDigest.getInstance(algorithm).apply {
            this@hash.inputStream().use {
                val buffer = ByteArray(1024)
                var read = it.read(buffer)

                while (read != -1) {
                    update(buffer, 0, read)
                    read = it.read(buffer)
                }
            }
        }.digest().joinToString("") { "%02x".format(it) }
    }
}