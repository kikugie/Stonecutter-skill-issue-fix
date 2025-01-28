package dev.kikugie.stonecutter.data.setup

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.tree.TreeBuilder
import dev.kikugie.stonecutter.validateId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal fun VersionConfiguration.toTree(builder: TreeBuilder) = builder.apply {
    if (vcs != null) vcsVersion = vcs
    nodes.putAll(entries.mapValues { (_, it) -> it.toMutableSet() })
    versions.putAll(entries.values.flatten().associateWith { it })
}

// TODO: Documentation
@Serializable(with = VersionConfiguration.JsonSerializer::class)
public sealed interface VersionConfiguration {
    public val vcs: Identifier?
    public val entries: Map<Identifier, List<StonecutterProject>>

    @Serializable
    public data class Source(
        override val vcs: Identifier? = null,
        override val entries: Map<Identifier, List<StonecutterProject>>
    ) : VersionConfiguration {
        init {
            entries.keys
                .filter(Identifier::isNotEmpty)
                .forEach(Identifier::validateId)
        }
    }

    @Serializable
    private data class BranchToVersions(
        override val vcs: Identifier? = null,
        val branches: Map<Identifier, ProjectList>
    ) : VersionConfiguration {
        init {
            branches.keys
                .filter(Identifier::isNotEmpty)
                .forEach(Identifier::validateId)
        }

        override val entries: Map<Identifier, List<StonecutterProject>>
            get() = branches.mapValues { (_, list) -> list.entries.map { it.entry} }
    }

    @Serializable
    private data class VersionsToBranches(
        override val vcs: Identifier? = null,
        val versions: Map<ExpandedProject.StringProject, BranchList>
    ) : VersionConfiguration {
        init {
            versions.entries.flatMap { it.value.entries }
                .filter(Identifier::isNotEmpty)
                .forEach(Identifier::validateId)
        }

        override val entries: Map<Identifier, List<StonecutterProject>>
            get() = buildMap<_, MutableList<StonecutterProject>> {
                for ((version, branches) in versions) branches.entries.forEach { branch ->
                    getOrPut(branch) { mutableListOf() } += version.entry
                }
            }
    }

    private object JsonSerializer : JsonContentPolymorphicSerializer<VersionConfiguration>(VersionConfiguration::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element !is JsonObject -> error("Element must be a JSON object")
            "branches" in element -> BranchToVersions.serializer()
            "versions" in element -> VersionsToBranches.serializer()
            "source" in element -> Source.serializer()
            else -> error("No version configuration type specified")
        }
    }
}

@Serializable(with = ExpandedProject.JsonSerializer::class)
internal sealed interface ExpandedProject {
    val entry: StonecutterProject

    @JvmInline @Serializable
    value class StringProject(val string: String) : ExpandedProject {
        override val entry: StonecutterProject
            get() = parseVersion(string.split(':', limit = 2))

        private fun parseVersion(it: List<String>) = when (it.size) {
            0 -> error("Empty strings are not allowed")
            1 -> StonecutterProject(it.first(), it.first())
            2 -> StonecutterProject(it.first(), it[1])
            else -> error("Unreachable")
        }
    }

    @Serializable
    data class CompositeProject(val project: String, val version: String = project) : ExpandedProject {
        override val entry: StonecutterProject
            get() = StonecutterProject(project, version)
    }

    object JsonSerializer : JsonContentPolymorphicSerializer<ExpandedProject>(ExpandedProject::class) {
        override fun selectDeserializer(element: JsonElement) = when(element) {
            is JsonObject -> CompositeProject.serializer()
            is JsonPrimitive -> StringProject.serializer()
            else -> error("Unsupported element type: ${element::class.simpleName}")
        }
    }
}

@Serializable(with = ProjectList.JsonSerializer::class)
internal sealed interface ProjectList {
    val entries: List<ExpandedProject>

    @JvmInline @Serializable
    value class PlainList(val list: List<ExpandedProject>) : ProjectList {
        override val entries: List<ExpandedProject>
            get() = list
    }

    @Serializable
    data class NestedList(val versions: List<ExpandedProject>) : ProjectList {
        override val entries: List<ExpandedProject>
            get() = versions
    }

    object JsonSerializer : JsonContentPolymorphicSerializer<ProjectList>(ProjectList::class) {
        override fun selectDeserializer(element: JsonElement) = when(element) {
            is JsonArray -> PlainList.serializer()
            is JsonObject -> NestedList.serializer()
            else -> error("Unsupported element type: ${element::class.simpleName}")
        }
    }
}

@Serializable(with = BranchList.JsonSerializer::class)
internal sealed interface BranchList {
    val entries: List<Identifier>

    @JvmInline @Serializable
    value class PlainList(val list: List<Identifier>) : BranchList {
        override val entries: List<Identifier>
            get() = list
    }

    @Serializable
    data class NestedList(val branches: List<Identifier>) : BranchList {
        override val entries: List<Identifier>
            get() = branches
    }

    object JsonSerializer : JsonContentPolymorphicSerializer<BranchList>(BranchList::class) {
        override fun selectDeserializer(element: JsonElement) = when(element) {
            is JsonArray -> PlainList.serializer()
            is JsonObject -> NestedList.serializer()
            else -> error("Unsupported element type: ${element::class.simpleName}")
        }
    }
}