import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.Yaml.Companion
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import java.io.File
import java.nio.file.Path

object ProjectFinder {
    fun find(file: File): String = find(file.inputStream().use { Yaml.default.decodeFromStream<SearchEntries>(it) }.entries)

    fun find(entries: List<SearchEntry>): String = runBlocking {
        val flow = entries.asFlow().transform {
            emit(ProjectInfo.Builder().apply {
                modrinth = it.modrinth
                curseforge = it.curseforge
                github = it.github

                ModrinthAPI.get(it)?.invoke(this)
                CurseforgeAPI.get(it)?.invoke(this)

                fun Any?.status() = if (this == null) "NO" else "OK"
                println("${it.name}: MR: ${modrinth.status()}, CF: ${curseforge.status()}, GH: ${github.status()}")
            }.build())
        }

        val projects = async {
            flow.toList()
                .sortedBy { -it.downloads }
                .joinToString("\n") { it.toJS() }
        }
        projects.await()
    }
}