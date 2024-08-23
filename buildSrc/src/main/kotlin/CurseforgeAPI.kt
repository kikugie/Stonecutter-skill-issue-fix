import kotlinx.serialization.Serializable


object CurseforgeAPI {
    private const val KEY =
        "$2a$10\$wuAJuNZuted3NORVmpgUC.m8sI.pv1tOPKZyBgLFGjxFp/br0lZCC" // Whatcha lookin' at, it's public anyway

    suspend fun get(entry: SearchEntry): BuilderConsumer? {
        val slug = entry.curseforge?.run { substringAfterLast('/') } ?: find(entry.name) ?: return null
        val link = "https://api.curseforge.com/v1/mods/$slug"
        val info = client.get<CfLookupResponse>(link, mapOf("x-api-key" to KEY))?.data ?: return null
        return {
            title = info.name
            description = info.summary
            downloads += info.downloadCount

            curseforge = info.links["websiteUrl"]
            info.links["sourceUrl"]?.let { github = it }
            info.logo?.let { icon = it.url }
        }
    }

    private fun find(mod: String): String? {
        val search = "https://api.curseforge.com/v1/mods/search?gameId=432&slug=$mod"
        return client.get<CfSearchResult>(search, mapOf("x-api-key" to KEY))?.data?.firstOrNull {
            mod.similarity(it.slug) >= accuracy
        }?.let {
            it.slug
        }
    }

    @Serializable
    private data class CfSearchResult(
        val data: List<CfProjectOverview>,
    )

    @Serializable
    private data class CfProjectOverview(
        val slug: String,
    )

    @Serializable
    private data class CfLogoInfo(
        val url: String
    )

    @Serializable
    private data class CfLookupResponse(
        val data: CfProjectInfo // This API sucks
    )

    @Serializable
    private data class CfProjectInfo(
        val name: String,
        val summary: String,
        val slug: String,
        val links: Map<String, String?>,
        val downloadCount: Int,
        val logo: CfLogoInfo?,
    )
}