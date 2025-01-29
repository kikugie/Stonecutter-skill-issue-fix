package dev.kikugie.stonecutter.data.tree

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.validateId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal fun TreeSettings.toTree(builder: TreeBuilder) = builder.apply {
    vcs?.let { vcsVersion = it }
    nodes.putAll(entries.mapValues { (_, it) -> it.toMutableSet() })
    versions.putAll(entries.values.flatten().associateWith { it })
}

/**
 * ## Intention
 * Class used to deserialize a data-driven project tree setup. The configuration is meant to be written by the user,
 * so it intentionally allows a choice between shorter or expanded syntax to allow encoding optional data for third-party tools.
 * You can use the [Stonecutter Versions](https://github.com/stonecutter-versioning/stonecutter/blob/0.6/tools/settings-schema.json)
 * JSON schema in your IDE to verify the syntax automatically.
 *
 * **Note**: The example comments use JSON5 formatting to allow comments, however, the real implementation only supports the standard JSON.
 *
 * ### Version specification
 * Suprojects are provided in several places across the available formats, so their syntax is described separately here,
 * and will have reference placeholders in other examples.
 *
 * - **String - `$[project-string]`**:
 *   This type is written as a string in `"project"` or `"project:version"` format.
 *   *Examples: `"1.20.1"`, `"1.21.4-fabric:1.21.4"`*.
 * - **Object - `$[project-object]`**:
 *   This type is declared with explicit fields:
 *   ```json5
 *   {
 *      "project": "...",
 *      "version": "..." // Optional
 *   }
 *   ```
 *   *Examples: `{"project": "1.20.1"}`, `{"project": "1.21.4-fabric", "version": "1.21.4"}`*.
 * - **Either - `$[project-either]`**:
 *   Allows using a mix of `$[project-string]` and `$[project-object]` formats.
 *
 * ### Single-branch setup
 * This setup provides shorter syntax for cases where only the main project branch is used.
 * ```json5
 * {
 *   "vcs": "...", // Optional: project part of version specification
 *   "versions": ["$[project-either]"]
 * }
 * ```
 *
 * ### Branches-first setup
 * ```json5
 * {
 *   "vcs": "...", // Optional: project part of version specification
 *   "branches": {
 *      "": ["$[project-either]"],
 *      "other": {
 *          // Allows nesting the version array for optional additional data
 *         "versions": ["$[project-either]"]
 *      }
 *   }
 * }
 * ```
 *
 * ### Versions-first setup
 * ```json5
 * {
 *   "vcs": "...", // Optional: project part of version specification
 *   "versions": {
 *      "$[project-string]": ["..."],
 *      "$[project-string]": {
 *          // Allows nesting the branch array for optional additional data
 *          "branches": ["..."]
 *      }
 *   }
 * }
 * ```
 */
@Serializable(with = TreeSettings.JsonSerializer::class)
public sealed interface TreeSettings {
    public val vcs: Identifier?
    public val entries: Map<Identifier, List<StonecutterProject>>

    @Serializable
    public data class Standard(
        override val vcs: Identifier? = null,
        override val entries: Map<Identifier, List<StonecutterProject>>
    ) : TreeSettings {
        init {
            entries.keys
                .filter(Identifier::isNotEmpty)
                .forEach(Identifier::validateId)
        }
    }

    @Serializable
    private data class BranchMap(
        override val vcs: Identifier? = null,
        val branches: Map<Identifier, ProjectList>
    ) : TreeSettings {
        init {
            branches.keys
                .filter(Identifier::isNotEmpty)
                .forEach(Identifier::validateId)
        }

        override val entries: Map<Identifier, List<StonecutterProject>>
            get() = branches.mapValues { (_, list) -> list.entries.map { it.entry} }
    }

    @Serializable
    private data class VersionMap(
        override val vcs: Identifier? = null,
        val versions: Map<ExpandedProject.StringProject, BranchList>
    ) : TreeSettings {
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

    @Serializable
    private data class VersionList(
        override val vcs: Identifier? = null,
        val versions: List<ExpandedProject>
    ): TreeSettings {
        override val entries: Map<Identifier, List<StonecutterProject>>
            get() = mapOf("" to versions.map { it.entry })
    }

    private object JsonSerializer : JsonContentPolymorphicSerializer<TreeSettings>(TreeSettings::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element !is JsonObject -> error("Element must be a JSON object")
            "source" in element -> Standard.serializer()
            "branches" in element -> BranchMap.serializer()
            "versions" in element -> when(element["versions"]) {
                is JsonArray -> VersionList.serializer()
                is JsonObject -> VersionMap.serializer()
                else -> error("Invalid version configuration type")
            }
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