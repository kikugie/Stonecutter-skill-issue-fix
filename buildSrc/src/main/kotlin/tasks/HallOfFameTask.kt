package tasks

import com.charleskorn.kaml.*
import com.github.ajalt.mordant.rendering.TextColors
import dev.kikugie.hall_of_fame.indentLines
import dev.kikugie.hall_of_fame.printStyled
import dev.kikugie.hall_of_fame.search.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

abstract class HallOfFameTask : DefaultTask() {
    private companion object {
        val YAML = Yaml(configuration = YamlConfiguration(
            polymorphismStyle = PolymorphismStyle.None,
            singleLineStringStyle = SingleLineStringStyle.Plain,
            ambiguousQuoteStyle = AmbiguousQuoteStyle.SingleQuoted,
            encodeDefaults = false
        ))

        fun <T> RegularFileProperty.readYaml(serializer: DeserializationStrategy<T>): T =
            get().asFile.inputStream().use { YAML.decodeFromStream(serializer, it) }

        fun <T> RegularFileProperty.writeYaml(serializer: SerializationStrategy<T>, value: T) = get().asFile.toPath().writeText(
            YAML.encodeToString(serializer, value),
            Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    @get:Input @get:Optional
    abstract val githubToken: Property<String>

    @get:InputFile
    abstract val configFile: RegularFileProperty

    @get:InputFile
    abstract val cacheFile: RegularFileProperty

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:Input
    abstract val outputFiles: Property<ConfigurableFileCollection>

    @TaskAction
    fun run() {
        val token = githubToken.orNull
        val config = configFile.readYaml(SearchConfig.serializer())
        val cache = if (cacheFile.get().asFile.readText().isBlank()) emptyList()
        else cacheFile.readYaml(ListSerializer(SearchEntry.serializer()))

        val (entries, projects) = runBlocking { Collector.get(token, config, cache) }
        entries.forEach { it.internal.keys.retainAll(listOf("curseforge_id")) }
        cacheFile.writeYaml(ListSerializer(SearchEntry.serializer()), entries.toList())

        val template = templateFile.get().asFile.readText()
        val js = projects.values
            .sortedByDescending { it.updated }
            .joinToString(",\n") { it.toJS() }
            .let { template.replaceFirst("'%PLACEHOLDER%'", it) }
        outputFiles.get().files.forEach { it.writeText(js) }
        writeUnresolved(entries.toList())
    }

    private fun writeUnresolved(entries: List<SearchEntry>) = entries.mapNotNull {
        if (!it.valid) return@mapNotNull null
        mutableMapOf<String, String>().apply {
            if (it.source.let { !it.isKnown && it !is Excluded }) this["source"] = "?"
            if (it.modrinth.let { !it.isKnown && it !is Excluded }) this["modrinth"] = "?"
            if (it.curseforge.let { !it.isKnown && it !is Excluded }) this["curseforge"] = "?"
        }.takeIf { it.isNotEmpty() }?.let { m->
            mutableMapOf("id" to it.id).apply {
                putAll(m)
            }
        }
    }.let {
        if (it.isNotEmpty()) printStyled(TextColors.brightYellow, YAML.encodeToString(it))
    }

    private fun ProjectInfo.toJS() = buildString {
        appendLine("{")
        appendLine(composeData().indentLines("  "))
        append("}")
    }

    private fun ProjectInfo.composeData() = buildString {
        appendLine("avatar:'${icon.escapeJS}',")
        appendLine("name:'${title.escapeJS}',")
        appendLine("title:'${description.escapeJS}',")
        composeLinks().takeIf(String::isNotEmpty)?.let {
            appendLine("links:[")
            appendLine(it.indentLines("  "))
            appendLine("],")
        }
        if (isNotEmpty()) setLength(length - 2) // Remove ,\n
    }

    private fun ProjectInfo.composeLinks() = buildString {
        modrinth?.run { appendLine("{icon:'modrinth',link:'$escapeJS' },") }
        curseforge?.run { appendLine("{icon:'curseforge',link:'$escapeJS'},") }
        source?.run { appendLine("{icon:'github',link:'$escapeJS'},") }
        if (isNotEmpty()) setLength(length - 2) // Remove ,\n
    }

    private val String.escapeJS: String
        get() = trim().replace('\n', ' ').replace("'", "\\'")
}