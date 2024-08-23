import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchEntry(
    val name: String,
    @SerialName("mr_link") val modrinth: String? = null,
    @SerialName("cf_link") val curseforge: String? = null,
    @SerialName("gh_link") val github: String? = null,
)

@Serializable
data class SearchEntries(
    val entries: List<SearchEntry>,
)