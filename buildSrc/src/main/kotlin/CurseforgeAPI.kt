import kotlinx.serialization.Serializable


object CurseforgeAPI {
    private const val KEY =
        "$2a$10\$wuAJuNZuted3NORVmpgUC.m8sI.pv1tOPKZyBgLFGjxFp/br0lZCC" // Whatcha lookin' at, it's public anyway

    fun get(entry: SearchEntry): BuilderConsumer? {
        val info = find(entry.name) ?: return null
        return {
            title = info.name
            description = info.summary
            downloads += info.downloadCount

            curseforge = info.links["websiteUrl"]
            info.links["sourceUrl"]?.let { github = it }
            info.logo?.let { icon = it.url }
        }
    }

    private fun find(mod: String): CfProjectInfo? {
        val template = "https://api.curseforge.com/v1/mods/search?gameId=432&%s=$mod"
        return info(template.format("slug"), mod) ?: info(template.format("searchFilter"), mod)
    }

    private fun info(link: String, mod: String) = client.get<CfSearchResult>(link, mapOf("x-api-key" to KEY))
        ?.data?.firstOrNull {
            mod.similarity(it.slug) >= accuracy
        }

    @Serializable
    private data class CfSearchResult(
        val data: List<CfProjectInfo>,
    )

    @Serializable
    private data class CfProjectInfo(
        val slug: String,
        val name: String,
        val summary: String,
        val links: Map<String, String?>,
        val downloadCount: Int,
        val logo: CfLogoInfo?,
    )

    @Serializable
    private data class CfLogoInfo(
        val url: String
    )

    @Serializable
    private data class CfLookupResponse(
        val data: CfProjectInfo // This API sucks
    )
}