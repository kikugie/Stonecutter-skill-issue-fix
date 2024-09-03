import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.red
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.eval.ConditionChecker
import dev.kikugie.stitcher.parser.CommentParser
import dev.kikugie.stitcher.transformer.TransformParameters
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object EvalTest {
    val SAMPLES = buildList {
        add("const && >=1 <=3", true) {
            dependencies[""] = "2"
            constants["const"] = true
        }
        add("const || const2", true) {
            constants["const"] = true
            constants["const2"] = false
        }
        add("!!!const", false) {
            constants["const"] = true
        }
        add("const1 && const2 || const3", true) {
            constants["const1"] = true
            constants["const2"] = false
            constants["const3"] = true
        }
        add("const1 && !(const2 || const3)", false) {
            constants["const1"] = true
            constants["const2"] = false
            constants["const3"] = true
        }
        add("!<=3 >1", false) {
            dependencies[""] = "2"
        }
    }

    @TestFactory
    fun `test eval`() = SAMPLES.map {
        DynamicTest.dynamicTest("'${it.first}' (${it.second})") { check(it.first, it.second, it.third) }
    }

    private fun check(expression: String, expected: Boolean, parameters: TransformParameters) {
        val parser = CommentParser.create(expression, params = parameters)
        val definition = parser.parse()
        Assertions.assertNotNull(definition)
        Assertions.assertIterableEquals(emptyList<String>(), parser.errors) {
            val errors = parser.errors.joinToString {
                "- ${red(it.second)} - ${cyan(it.first.toString())}"
            }
            "Unexpected errors:\n$errors\n"
        }

        val checker = ConditionChecker(parameters)
        Assertions.assertEquals(expected, definition!!.accept(checker)) { "'$expression' evaluated to wrong result" }
    }

    private inline fun MutableList<Triple<String, Boolean, TransformParameters>>.add(expression: String, result: Boolean, parameters: TransformParametersBuilder.() -> Unit) {
        add(Triple("? $expression", result, TransformParametersBuilder().apply(parameters).build()))
    }

    private class TransformParametersBuilder {
        val swaps: MutableMap<String, String> = mutableMapOf()
        val constants: MutableMap<String, Boolean> = mutableMapOf()
        val dependencies: MutableMap<String, String> = mutableMapOf()

        fun build() = TransformParameters(swaps, constants, dependencies.mapValues { VersionParser.parse(it.value) })
    }
}