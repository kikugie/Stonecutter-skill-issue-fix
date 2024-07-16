data class ProjectInfo(
    val title: String,
    val description: String,
    val icon: String,
    val downloads: Int,
    val modrinth: String?,
    val curseforge: String?,
    val github: String?,
) {
    fun toJS(): String {
        val data = composeData().ind(2)
        return buildString {
            append("{\n")
            append(data)
            append("\n}")
        }.ind(2)
    }

    private fun composeData(): String {
        val entries = mutableListOf<String>()
        entries += "avatar: '${icon.fix()}'"
        entries += "name: '${title.fix()}'"
        entries += "title: '${description.fix()}'"
        val links = composeLinks()
        if (links.isNotBlank()) entries += buildString {
            append("links: [\n")
            append(links.ind(2))
            append("\n]")
        }
        return entries.joinToString(",\n")
    }

    private fun composeLinks(): String {
        val links = mutableListOf<String>()
        if (github != null) links += "{ icon: 'github', link: '${github.fix()}' }"
        if (modrinth != null) links += "{ icon: { svg: modrinth }, link: '${modrinth.fix()}' }"
        if (curseforge != null) links += "{ icon: { svg: curseforge }, link: '${curseforge.fix()}' }"
        return links.joinToString(",\n")
    }

    private fun String.ind(n: Int) = lines().joinToString("\n") { " ".repeat(n) + it }
    private fun String.fix() = trim().replace('\n', ' ').replace("'", "\\'")

    class Builder {
        lateinit var title: String
        lateinit var description: String
        lateinit var icon: String
        var downloads: Int = 0
        var modrinth: String? = null
        var curseforge: String? = null
        var github: String? = null
        inline operator fun invoke(action: Builder.() -> Unit) = apply(action)
        fun build() = ProjectInfo(title, description, icon, downloads, modrinth, curseforge, github)
        fun tryBuild() = runCatching { build() }
    }
}
