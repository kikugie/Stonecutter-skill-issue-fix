import dev.kikugie.stitcher.parser.CommentParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import com.github.ajalt.mordant.rendering.TextColors.*

object CommentParserTest {
    val SUGAR_TESTS = listOf(
        "? if" to false,
        "? if const" to true,
        "?} if" to false,
        "?} if const" to false,

        "? else" to false,
        "? else const" to false,
        "?} else" to true,
        "?} else const" to false,

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

    @TestFactory
    fun `test sugar`() = SUGAR_TESTS.tests()
    @TestFactory
    fun `test swap`() = SWAP_TESTS.tests()
    @TestFactory
    fun `test condition`() = CONDITION_TESTS.tests()
    @TestFactory
    fun `test scope`() = SCOPE_TESTS.tests()

    private fun Iterable<Pair<String, Boolean>>.tests() = map {
        val str = if (it.second) '+' else '-'
        DynamicTest.dynamicTest("'${it.first}'($str)") { check(it.first, it.second) }
    }

    private fun check(input: String, succeeds: Boolean) {
        val parser = CommentParser.create(input)
        val result = parser.parse()
        Assertions.assertNotNull(result) { "Failed to parse $input" }

        val errors = parser.errors.joinToString {
            "- ${red(it.second)} - ${cyan(it.first.toString())}"
        }
        if (!succeeds) {
            Assertions.assertFalse(parser.errors.isEmpty()) { "Expected to have errors" }
            println(green("'$input' received errors:\n$errors\n"))
        } else Assertions.assertIterableEquals(emptyList<String>(), parser.errors) {
            "Unexpected errors:\n$errors\n"
        }
    }
}