@file:OptIn(ExperimentalSerializationApi::class)

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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