import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.commons.text.similarity.LevenshteinDistance
import kotlin.math.roundToInt

object ProjectFinder {
    val client = HttpUtils { IllegalStateException(it.message) }
    val accuracy = 0.8

    fun find(vararg mods: String): List<ProjectInfo> = mods
            .map(::find)
            .mapNotNull(Result<ProjectInfo>::getOrNull)
            .toList()

    fun find(slug: String): Result<ProjectInfo> {
        val builder = ProjectInfo.Builder()
        val cf = findCurseforge(slug, builder)
        val mr = findModrinth(slug, builder)
        val str = buildString {
            append("Searching for '$slug': ")
            append("Modrinth: ")
            if (mr != null) append("${mr.slug}|${(mr.similarity * 100).toInt()}%")
            else append("X")
            append(' ')
            append("Curseforge: ")
            if (cf != null) append("${cf.slug}|${(cf.similarity * 100).toInt()}%")
            else append("X")
        }
        println(str)

        return builder.tryBuild()
    }

    private fun findCurseforge(mod: String, builder: ProjectInfo.Builder): StatResult? {
        val key = "$2a$10\$wuAJuNZuted3NORVmpgUC.m8sI.pv1tOPKZyBgLFGjxFp/br0lZCC" // Whatcha lookin' at, it's public anyway
        var similarity: Double = .0
        val search = "https://api.curseforge.com/v1/mods/search?gameId=432&slug=$mod"
        val info = client.get<CfSearchResult>(search, mapOf("x-api-key" to key)).data.firstOrNull {
            similarity = mod.similarity(it.slug)
            similarity >= accuracy
        } ?: return null
        builder {
            title = info.name
            description = info.summary
            downloads += info.downloadCount

            curseforge = info.links["websiteUrl"]
            info.links["sourceUrl"]?.let { github = it }
            info.logo?.let { icon = it.url }
        }
        return StatResult(similarity, info.slug)
    }

    private fun findModrinth(mod: String, builder: ProjectInfo.Builder): StatResult? {
        val search = "https://api.modrinth.com/v2/search?query=$mod&facets=[[\"project_type:mod\"]]"
        var similarity: Double = .0
        val result = client.get<MrSearchResult>(search).hits.firstOrNull{
            similarity = mod.similarity(it.slug)
            similarity >= accuracy
        } ?: return null
        val project = "https://api.modrinth.com/v2/project/${result.slug}"
        val info = client.get<MrProjectInfo>(project)
        builder {
            title = info.title
            description = info.description
            downloads += info.downloads
            icon = info.iconUrl

            modrinth = "https://modrinth.com/mod/${info.slug}"
            info.sourceUrl?.let { github = it }
        }
        return StatResult(similarity, info.slug)
    }

    private fun String.raw() = lowercase()
        .replace("-", "")
        .replace("_", "")
        .replace(" ", "")

    private fun String.similarity(other: String): Double {
        val r1 = raw()
        val r2 = other.raw()
        val distance = LevenshteinDistance().apply(r1, r2)
        return 1.0 - distance / kotlin.math.max(r1.length, r2.length).toDouble()
    }

    @Serializable
    private data class MrSearchResult(
        val hits: List<MrProjectOverview>,
    )

    @Serializable
    private data class MrProjectOverview(
        val slug: String,
    )

    @Serializable
    private data class MrProjectInfo(
        val slug: String,
        val downloads: Int,
        @SerialName("icon_url")
        val iconUrl: String,
        @SerialName("source_url")
        val sourceUrl: String? = null,
        val title: String,
        val description: String,
    )

    @Serializable
    private data class CfProjectOverview(
        val name: String,
        val summary: String,
        val slug: String,
        val links: Map<String, String?>,
        val downloadCount: Int,
        val logo: CfLogoInfo?,
    )

    @Serializable
    private data class CfSearchResult(
        val data: List<CfProjectOverview>,
    )

    @Serializable
    private data class CfLogoInfo(
        val url: String
    )

    private data class StatResult(
        val similarity: Double,
        val slug: String
    )
}