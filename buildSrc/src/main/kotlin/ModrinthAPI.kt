import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object ModrinthAPI {
    suspend fun get(entry: SearchEntry): BuilderConsumer? {
        val slug = entry.modrinth?.run { substringAfterLast('/') } ?: find(entry.name) ?: return null
        val link = "https://api.modrinth.com/v2/project/$slug"
        val info = client.get<MrProjectInfo>(link) ?: return null
        return {
            title = info.title
            description = info.description
            downloads += info.downloads
            icon = info.iconUrl

            modrinth = "https://modrinth.com/mod/${info.slug}"
            info.sourceUrl?.let { github = it }
        }
    }

    private fun find(mod: String): String? {
        val search = "https://api.modrinth.com/v2/search?query=$mod&facets=[[\"project_type:mod\"]]"
        return client.get<MrSearchResult>(search)?.hits?.firstOrNull{
            mod.similarity(it.slug) >= accuracy
        }?.let {
            it.slug
        }
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
}