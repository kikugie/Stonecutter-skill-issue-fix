import dev.kikugie.semver.VersionParser
import dev.kikugie.semver.VersionParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object SemverTest {
    val CORRECT_SAMPLES = listOf(
        "1", "1.0", "1.0.0", "1.0.0.0", "1.0-2.1",
        "1.0-alpha", "1.0-super-alpha", "1.0-alpha.1",
        "1.0+build", "1.0+super-build", "1.0+build.1",
        "1.0-alpha+build", "1.0-alpha.1+build.1",
        "1.0.0-alpha+001", "1.0.0+20130313144700", "1.0.0-beta+exp.sha.5114f85", "1.0.0+21AF26D3----117B344092BD"
    )

    val INCORRECT_SAMPLES = listOf(
        "", "A", ".", "1.", "1-", "1+", "1-+", "1--", "1++",
        "1..0", ".1", "1,0", "1.0-a|pha", "1.0+a|pha"
    )

    @TestFactory
    fun `test correct semver`() = CORRECT_SAMPLES.map {
        DynamicTest.dynamicTest("Check $it") {
            VersionParser.parse(it, full = true)
        }
    }

    @TestFactory
    fun `test incorrect semver`() = INCORRECT_SAMPLES.map {
        DynamicTest.dynamicTest("Check $it") {
            Assertions.assertThrows(VersionParsingException::class.java) { VersionParser.parse(it, full = true) }
        }
    }
}