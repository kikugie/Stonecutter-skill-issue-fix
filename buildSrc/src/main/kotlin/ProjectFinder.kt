import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.mordant.rendering.TextColors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.File

object ProjectFinder {
    private const val EXCLUDE = "EXCLUDE"
    private val OK = green("OK")
    private val FIX = brightYellow("FX")
    private val CUSTOM = cyan("CT")
    private val EXCLUDED = gray("EX")
    private val ERROR = red("ER")

    fun find(file: File): String =
        find(file.inputStream().use { Yaml.default.decodeFromStream<SearchEntries>(it) }.entries)

    fun find(entries: List<SearchEntry>): String = runBlocking {
        println(brightGreen("Searching ${entries.size} entries..."))
        val longest = entries.maxOf { it.name.length }
        val template = "| %s | %s | %s | %s |"
        val header = "| ${"Name".padEnd(longest)} | MR | CF | GH |"
        val divider = "+${"-".repeat(longest + 2)}+----+----+----+"
        println(divider)
        println(header)
        println(divider)

        val flow = entries.asFlow().flowOn(Dispatchers.Default).transform {
            emit(ProjectInfo.Builder().apply {
                if (it.curseforge != EXCLUDE) CurseforgeAPI.get(it)?.invoke(this)
                if (it.modrinth != EXCLUDE) ModrinthAPI.get(it)?.invoke(this)

                it.title?.let { title = it }
                it.icon?.let { icon = it }
                it.description?.let { description = it }
                downloads = (downloads + it.adjust).coerceAtMost(Int.MAX_VALUE)

                fun Any?.status(replaced: Any?) = when {
                    replaced == EXCLUDE -> EXCLUDED
                    this != null && replaced != null -> FIX
                    this == null && replaced == null -> ERROR
                    this == null && replaced != null -> CUSTOM
                    else -> OK
                }
                println(
                    template.format(
                        it.name.padEnd(longest),
                        modrinth.status(it.modrinth),
                        curseforge.status(it.curseforge),
                        github.status(it.github),
                    )
                )

                it.modrinth.takeUnless { it == EXCLUDE }?.let { modrinth = it }
                it.curseforge.takeUnless { it == EXCLUDE }?.let { curseforge = it }
                it.github?.let { github = it }
            }.tryBuild().getOrNull())
        }.onCompletion {
            println(divider)
        }.filterNotNull()


        val projects = flow.toList()
            .also { println(brightGreen("Sorting ${entries.size} entries...")) }
            .sortedBy { -it.downloads }
            .joinToString(",\n") { it.toJS() }
        projects
    }
}