import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.File

object ProjectFinder {
    fun find(file: File): String =
        find(file.inputStream().use { Yaml.default.decodeFromStream<SearchEntries>(it) }.entries)

    fun find(entries: List<SearchEntry>): String = runBlocking {
        val longest = entries.maxOf { it.name.length }
        val template = "| %s | %s | %s | %s |"
        val header = "| ${"Name".padEnd(longest)} | MR | CF | GH |"
        val divider = "+${"-".repeat(longest + 2)}+----+----+----+"
        println(divider)
        println(header)
        println(divider)

        val flow = entries.asFlow().flowOn(Dispatchers.Default).transform {
            emit(ProjectInfo.Builder().apply {
                CurseforgeAPI.get(it)?.invoke(this)
                ModrinthAPI.get(it)?.invoke(this)

                it.title?.let { title = it }
                it.icon?.let { icon = it }
                it.description?.let { description = it }
                downloads = (downloads + it.adjust).coerceAtMost(Int.MAX_VALUE)

                fun Any?.status(replaced: Any?) = when {
                    this != null && replaced != null -> TextColors.brightYellow("FX")
                    this == null && replaced == null -> TextColors.red("NO")
                    this == null && replaced != null -> TextColors.cyan("OT")
                    else -> TextColors.green("OK")
                }
                println(
                    template.format(
                        it.name.padEnd(longest),
                        modrinth.status(it.modrinth),
                        curseforge.status(it.curseforge),
                        github.status(it.github),
                    )
                )

                it.modrinth?.let { modrinth = it }
                it.curseforge?.let { curseforge = it }
                it.github?.let { github = it }
            }.tryBuild().getOrNull())
        }.onCompletion {
            println(divider)
        }.filterNotNull()

        val projects = flow.toList()
            .sortedBy { -it.downloads }
            .joinToString(",\n") { it.toJS() }
        projects
    }
}