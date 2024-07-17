import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.commons.text.similarity.LevenshteinDistance

object ProjectFinder {
    val client = HttpUtils { IllegalStateException(it.message) }
    val accuracy = 0.9

    fun find(vararg mods: String): List<ProjectInfo> = mods
            .map(::find)
            .mapNotNull(Result<ProjectInfo>::getOrNull)
            .toList()

    fun find(mrSlug: String, cfSlug: String = mrSlug): Result<ProjectInfo> {
        println("Retrieving info for '$mrSlug'")
        val builder = ProjectInfo.Builder()
        findCurseforge(cfSlug, builder)
        findModrinth(mrSlug, builder)
        return builder.tryBuild()
    }

    private fun findCurseforge(mod: String, builder: ProjectInfo.Builder) {
        val key = "$2a$10\$wuAJuNZuted3NORVmpgUC.m8sI.pv1tOPKZyBgLFGjxFp/br0lZCC" // Whatcha lookin' at, it's public anyway
        val search = "https://api.curseforge.com/v1/mods/search?gameId=432&slug=$mod"
        val info = client.get<CfSearchResult>(search, mapOf("x-api-key" to key)).data.firstOrNull {
            mod.similarity(it.slug) >= accuracy
        } ?: return
        builder {
            title = info.name
            description = info.summary
            downloads += info.downloadCount

            curseforge = info.links["websiteUrl"]
            info.links["sourceUrl"]?.let { github = it }
            info.logo?.let { icon = it.url }
        }
    }

    private fun findModrinth(mod: String, builder: ProjectInfo.Builder) {
        val search = "https://api.modrinth.com/v2/search?query=$mod&facets=[[\"project_type:mod\"]]"
        val result = client.get<MrSearchResult>(search).hits.firstOrNull{
            mod.similarity(it.slug) >= accuracy
        } ?: return
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
}