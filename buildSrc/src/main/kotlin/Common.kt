import org.apache.commons.text.similarity.LevenshteinDistance
import kotlin.math.max

internal typealias BuilderConsumer = ProjectInfo.Builder.() -> Unit

internal val client = HttpUtils()
internal val accuracy = 0.8

private fun String.raw() = lowercase()
    .replace("-", "")
    .replace("_", "")
    .replace(" ", "")

internal fun String.similarity(other: String): Double {
    val r1 = raw()
    val r2 = other.raw()
    val distance = LevenshteinDistance().apply(r1, r2)
    return 1.0 - distance / max(r1.length, r2.length).toDouble()
}