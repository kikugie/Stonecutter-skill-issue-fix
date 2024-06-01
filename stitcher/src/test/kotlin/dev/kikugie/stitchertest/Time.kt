package dev.kikugie.stitchertest

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.process.FileParser
import dev.kikugie.stitcher.process.Lexer
import org.junit.jupiter.api.Test
import java.security.MessageDigest

object Time {
    @Test
    fun test() {
        val version = "1.21-beta.1"
        val semver = SemanticVersionParser.parse(version)
        println(semver)
    }

    private fun createParser(str: String): FileParser {
        return FileParser(str.reader(), recognizers)
    }

    private fun createLexer(str: CharSequence): Lexer {
        val handler = ErrorHandlerImpl(str)
        return Lexer(str, handler)
    }

    private fun String.hash(algorithm: String): String = MessageDigest.getInstance(algorithm).apply {
        this@hash.byteInputStream().use {
            val buffer = ByteArray(1024)
            var read = it.read(buffer)

            while (read != -1) {
                update(buffer, 0, read)
                read = it.read(buffer)
            }
        }
    }.digest().joinToString("") { "%02x".format(it) }
}