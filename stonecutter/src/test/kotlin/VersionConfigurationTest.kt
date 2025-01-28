import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.setup.VersionConfiguration
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object VersionConfigurationTest {
    private val SAMPLES = buildList {
        val default = buildMap {
            val test = listOf(
                StonecutterProject("1.20.1", "1.20.1"),
                StonecutterProject("1.21.1", "ok")
            )
            put("test", test)
        }
        test("plain-minimal") {
            input = """
                {
                  "branches": {
                    "test": ["1.20.1", "1.21.1:ok"]
                  }
                }
            """.trimIndent()
            output = default
        }
        test("plain-full") {
            input = """
                {
                  "branches": {
                    "test": {
                      "versions": [
                        {
                          "project": "1.20.1"
                        },
                        {
                          "project": "1.21.1",
                          "version": "ok"
                        } 
                      ]     
                    }
                  }
                }
            """.trimIndent()
            output = default
        }
        test("reversed-minimal") {
            input = """
                {
                  "versions": {
                    "1.20.1": ["test"],
                    "1.21.1:ok": ["test"]
                  }
                }
            """.trimIndent()
            output = default
        }

        test("reversed-full") {
            input = """
                {
                  "versions": {
                    "1.20.1": {
                      "branches": ["test"]
                    },
                    "1.21.1:ok": {
                      "branches": ["test"]
                    }
                  }
                }
            """.trimIndent()
            output = default
        }
    }

    @TestFactory
    fun `test json`() = SAMPLES.map {
        DynamicTest.dynamicTest(it.name) { check(it) }
    }

    private fun check(entry: TestEntry) {
        val parsed = Json.decodeFromString<VersionConfiguration>(entry.input)
        Assertions.assertEquals(entry.output, parsed.entries)
    }

    private inline fun MutableList<TestEntry>.test(name: String, action: TestEntry.() -> Unit) {
        add(TestEntry().apply { this.name = name }.apply(action))
    }

    private class TestEntry {
        lateinit var name: String
        @Language("JSON") lateinit var input: String
        lateinit var output: Map<Identifier, List<StonecutterProject>>
    }
}