package dev.kikugie.stitchertest

import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream

object Time {
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