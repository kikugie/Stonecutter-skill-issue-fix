import dev.kikugie.stitcher.eval.join
import dev.kikugie.stitcher.parser.CommentParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object AssemblerTest {
    val SAMPLES = sequence {
        append(CommentParserTest.SUGAR_TESTS)
        append(CommentParserTest.SWAP_TESTS)
        append(CommentParserTest.CONDITION_TESTS)
        append(CommentParserTest.SCOPE_TESTS)
    }

    @TestFactory
    fun `test assembler`() = SAMPLES.map {
        DynamicTest.dynamicTest(it) { check(it) }
    }.asIterable()

    private fun check(input: String) {
        val parser = CommentParser.create(input)
        val result = parser.parse()
        Assertions.assertNotNull(result) { "Failed to parse $input" }
        Assertions.assertEquals(input.trim(), result!!.join())
    }

    private suspend fun SequenceScope<String>.append(elements: Iterable<Pair<String, Boolean>>) = elements.forEach {
        if (it.second) yield(it.first)
    }
}