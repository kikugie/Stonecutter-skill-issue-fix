import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchEntry(
    val name: String,
    val adjust: Int = 0,
    val title: String? = null,
    val icon: String? = null,
    val description: String? = null,
    @SerialName("mr_link") val modrinth: String? = null,
    @SerialName("cf_link") val curseforge: String? = null,
    @SerialName("gh_link") val github: String? = null,
)

@Serializable
data class SearchEntries(
    val entries: List<SearchEntry>,
)