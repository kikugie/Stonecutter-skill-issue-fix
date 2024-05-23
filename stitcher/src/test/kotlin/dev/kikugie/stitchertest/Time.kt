package dev.kikugie.stitchertest

import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.CommentParser
import dev.kikugie.stitcher.process.Lexer
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream

object Time {
    @Test
    fun test() {
        val input = "$ if !identifier: >1.20.2 24w30a && bl1 || (bl2 && bl1) >>"
        println("Input:  $input")
        val parser = createParser(input)
        val result = parser.parse()
        println("Output: ${result?.accept(Assembler)}")
//        println(result.yaml())

//        val lexer = createLexer(input)
//        while (lexer.advance() != null) {
//            println(lexer.lookup())
//        }
    }

    private fun createParser(str: CharSequence): CommentParser {
        val handler = ErrorHandlerImpl(str)
        val lexer = Lexer(str, handler)
        return CommentParser(lexer, handler)
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