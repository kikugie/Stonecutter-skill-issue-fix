import dev.kikugie.stitcher.parser.CommentParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import com.github.ajalt.mordant.rendering.TextColors.*
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.token.StitcherTokenType
import dev.kikugie.stitcher.data.token.WhitespaceType
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.Lexer

object CommentParserTest {
    val SUGAR_TESTS = listOf(
        "? if" to false,
        "? if const" to true,
        "?} if" to false,
        "?} if const" to false,

        "? else" to false,
        "? else const" to false,
        "?} else" to true,
        "?} else const" to true,

        "? elif" to false,
        "? elif const" to false,
        "?} elif" to false,
        "?} elif const" to true,

        "? else if" to false,
        "? else if const" to false,
        "?} else if" to false,
        "?} else if const" to true,

        "? if if const" to false,
    )

    val SWAP_TESTS = listOf(
        "$ {" to false,
        "$ identifier" to true,
        "$ identifier {" to true,
        "$ identifier { extra" to false,
        "$ identifier identifier2" to false,
        "$}" to true,
        "$} identifier" to false,
        "$} identifier {" to false,
        "$} {" to false,
    )

    val CONDITION_TESTS = listOf(
        "? const" to true,
        "? const const" to false,
        "? const && const" to true,
        "? const && const && const" to true,
        "? const || const" to true,
        "? const || (const && const)" to true,
        "? !const" to true,
        "? !(const && const)" to true,
        "? !const || const" to true,
        "? const! || const" to false,
        "? const && !(group)" to true,
        "? (const" to false,
        "? const)" to false,
        "? const ||" to false,
        "? || const" to false,
        "? const && || const" to false,
        "? 1" to true,
        "? >=1" to true,
        "? >=1 <=3" to true,
        "? !>=1 <=3" to true,
        "? dependency: >=1 <=3" to true,
        "? dependency: >=1 <=3 && const" to true,
        "? dependency: >=1 <=3 const" to false,
        "? dependency:" to false,
        "? dependency: dependency" to false,
        "? dependency: !>=1 <=3" to false,
        "? !dependency: >=1 <=3" to true,
    )

    val SCOPE_TESTS = listOf(
        "? const" to true,
        "? const {" to true,
        "? const >>" to true,
        "? {" to false,
        "? >>" to false,
        "?}" to true,
        "?} {" to true,
        "?} >>" to true,
        "? const {  " to true,
        "? const { const again" to false,
    )

    val MISC_TESTS = listOf(
        " ? const" to true,
        "? >= 1" to true,
        "? hi my name is multiplier, no - market flyer, no - amplifier, no - marble fire" to false
    )

    val LEXER_TESTS = listOf(
        "? =vers vers" to listOf(MarkerType.CONDITION, WhitespaceType, StitcherTokenType.PREDICATE, WhitespaceType, StitcherTokenType.IDENTIFIER),
    )

    @TestFactory
    fun `test sugar`() = SUGAR_TESTS.tests()
    @TestFactory
    fun `test swap`() = SWAP_TESTS.tests()
    @TestFactory
    fun `test condition`() = CONDITION_TESTS.tests()
    @TestFactory
    fun `test scope`() = SCOPE_TESTS.tests()
    @TestFactory
    fun `misc space`() = MISC_TESTS.tests()
    @TestFactory
    fun `lexer tests`() = LEXER_TESTS.map {
        DynamicTest.dynamicTest(it.first) {
            val lexer = Lexer(it.first, StoringErrorHandler())
            lexer.all().forEach { println(it) }
            val types = lexer.all().map { it.type }
            Assertions.assertIterableEquals(it.second, types)
        }
    }

    private fun Iterable<Pair<String, Boolean>>.tests() = map {
        val str = if (it.second) '+' else '-'
        DynamicTest.dynamicTest("'${it.first}'($str)") { check(it.first, it.second) }
    }

    private fun check(input: String, succeeds: Boolean) {
        val handler = StoringErrorHandler()
        val parser = CommentParser.create(input, handler)
        val result = parser.parse()
        Assertions.assertNotNull(result) { "Failed to parse $input" }

        val errors = handler.errors.joinToString("\n") {
            "- ${red(it.second)} - ${cyan(it.first.toString())}"
        }
        if (!succeeds) {
            Assertions.assertFalse(handler.errors.isEmpty()) { "Expected to have errors" }
            println(green("'$input' received errors:\n$errors\n"))
        } else Assertions.assertIterableEquals(emptyList<String>(), handler.errors) {
            "Unexpected errors:\n$errors\n"
        }
    }
}