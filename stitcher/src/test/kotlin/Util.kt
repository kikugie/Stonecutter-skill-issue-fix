import com.charleskorn.kaml.Yaml
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.TokenType
import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stitcher.scanner.Scanner
import kotlinx.serialization.encodeToString

fun String.scan() = Scanner(this, CommentRecognizers.ALL).asIterable()

fun MutableList<Token>.token(value: String, type: TokenType) {
    add(Token(value, type))
}

fun Sequence<Token>.yaml() = toList().yaml()
fun List<Token>.yaml() = Yaml.default.encodeToString(this)
inline fun <reified T> T.yaml() = Yaml.default.encodeToString(this)