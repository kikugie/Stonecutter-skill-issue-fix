package dev.kikugie.stonecutter.test

import dev.kikugie.stonecutter.processor.CommentProcessor
import dev.kikugie.stonecutter.processor.ConditionProcessor
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.junit5.JUnit5Asserter.assertEquals
import kotlin.test.junit5.JUnit5Asserter.fail

object ProcessorTest {
    private fun resource(file: String) = ProcessorTest::class.java.classLoader.getResourceAsStream(file)?.bufferedReader() ?:
        throw FileNotFoundException(file)
    private fun processFile(file: String) {
        try {
            val sample = resource("samples/$file")
            val expected = resource("expected/$file").readText()
            val result = CommentProcessor.process(sample, ConditionProcessor.TEST).first.toString()

            assertEquals("Incorrect processing", expected, result)
        } catch (e: Exception) {
            fail(e.stackTraceToString())
        }
    }

    @Test
    fun `single line`() {
        processFile("singleline.kts")
    }

    @Test
    fun `if else chain`() {
        processFile("ifelsechain.kts")
    }
}