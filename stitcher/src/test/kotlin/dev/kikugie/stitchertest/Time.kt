package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.RootScope
import dev.kikugie.stitcher.data.Scope
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import net.orandja.obor.codec.Cbor
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.reader
import kotlin.io.path.writeBytes

object Time {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
    @Test
    fun test() {
        val path = Path("E:\\IdeaProjects\\re-bread\\src\\main\\kotlin\\dev\\kikugie\\elytratrims\\common\\access\\FeatureAccess.kt")
        val path2 = Path("E:\\IdeaProjects\\Stonecutter-skill-issue-fix\\stitcher\\src\\test\\kotlin\\dev\\kikugie\\stitchertest\\exp")
        val ast = path.reader().scan(recognizers).lex().parse()
        val ser = Cbor.encodeToByteArray(Scope.serializer(), ast)
        val hash = path.hash("MD5")
        val dest = path2.resolve("$hash.ast")
        dest.writeBytes(ser, StandardOpenOption.CREATE)
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