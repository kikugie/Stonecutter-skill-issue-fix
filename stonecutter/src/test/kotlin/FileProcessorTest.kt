import dev.kikugie.stonecutter.process.DirectoryData
import kotlin.io.path.Path

object FileProcessorTest {
    val ROOT = Path("/home/kikugie/IdeaProjects/stonecutter/stonecutter/src/test/resources")

    fun `test performance`() {
        val dirs = DirectoryData(
            ROOT.resolve("src"),
            ROOT.resolve("dest"),
            ROOT.resolve("cacheIn"),
            ROOT.resolve("cacheOut")
        )
    }
}