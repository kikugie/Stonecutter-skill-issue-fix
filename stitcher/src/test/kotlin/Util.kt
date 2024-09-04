import com.charleskorn.kaml.Yaml
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.TokenType
import dev.kikugie.stitcher.scanner.HashSingleLine
import dev.kikugie.stitcher.scanner.Scanner
import dev.kikugie.stitcher.scanner.StandardMultiLine
import dev.kikugie.stitcher.scanner.StandardSingleLine
import dev.kikugie.stitcher.transformer.TransformParameters
import kotlinx.serialization.encodeToString

val RECOGNIZERS = listOf(StandardSingleLine, HashSingleLine, StandardMultiLine)

fun String.scan() = Scanner(reader(), RECOGNIZERS).tokenize()

fun MutableList<Token>.token(value: String, type: TokenType) {
    add(Token(value, type))
}

fun Sequence<Token>.yaml() = toList().yaml()
fun List<Token>.yaml() = Yaml.default.encodeToString(this)
inline fun <reified T> T.yaml() = Yaml.default.encodeToString(this)

inline fun TransformParameters(build: TransformParametersBuilder.() -> Unit) = TransformParametersBuilder().apply(build).build()

class TransformParametersBuilder {
    val swaps: MutableMap<String, String> = mutableMapOf()
    val constants: MutableMap<String, Boolean> = mutableMapOf()
    val dependencies: MutableMap<String, String> = mutableMapOf()

    fun build() = TransformParameters(swaps, constants, dependencies.mapValues { VersionParser.parse(it.value) })
}