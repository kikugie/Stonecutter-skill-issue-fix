package dev.kikugie.stitchertest

import dev.kikugie.semver.SemanticVersionParser
import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.process.*
import dev.kikugie.stitcher.process.transformer.Container
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream

object Time {
    @Test
    fun test() {
        val input = """
            //? if >1.0.0
            xd
        """.trimIndent()
        println("Input:\n$input\n")
//        val result = input.scan()
        val parser = createParser(input)
        val result = parser.parse()

        val trans = Transformer(result, recognizers, Container(
            swaps = mapOf(),
            dependencies = mapOf(Token.EMPTY.value to SemanticVersionParser.parse("0.1.0")),
            constants = mapOf()
        ))
        trans.process()
        println(result.yaml())
        println("\n")
        println(result.accept(Assembler))
    }

    private fun createParser(str: String): FileParser {
        return FileParser(str.reader(), recognizers)
    }

    private fun createLexer(str: CharSequence): Lexer {
        val handler = ErrorHandlerImpl(str)
        return Lexer(str, handler)
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