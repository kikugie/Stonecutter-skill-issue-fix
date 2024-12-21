@file: UseSerializers(PathSerializer::class)

package dev.kikugie.stonecutter.data.tree

import com.charleskorn.kaml.Yaml
import dev.kikugie.stonecutter.StonecutterAPI
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stitcher.util.PathSerializer
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private fun <T> save(location: Path, model: T, serializer: KSerializer<T>): Result<Unit> = location.runCatching {
    val yaml = Yaml.default.encodeToString(serializer, model)
    parent.createDirectories()
    writeText(
        yaml,
        Charsets.UTF_8,
        StandardOpenOption.WRITE,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
    )
}

private fun <T> load(location: Path, serializer: KSerializer<T>): Result<T> = location.runCatching {
    if (location.notExists()) throw NoSuchFileException(location.toFile())
    val text = readText(Charsets.UTF_8)
    Yaml.default.decodeFromString(serializer, text)
}

@Serializable
data class NodeInfo(
    val metadata: StonecutterProject,
    val path: Path,
    val active: Boolean = false,
)

@Serializable
data class BranchInfo(
    val id: String,
    val path: Path,
)

@Serializable
data class NodeModel(
    val metadata: StonecutterProject,
    val root: Path,
    val branch: BranchInfo,
    val active: Boolean,
    val parameters: BuildParameters
) {
    companion object {
        const val FILENAME = "node.yml"

        @JvmStatic @StonecutterAPI
        fun load(directory: Path): Result<NodeModel> =
            load(directory.resolve(FILENAME), serializer())
    }

    internal fun save(directory: Path): Result<Unit> =
        save(directory.resolve(FILENAME), this, serializer())
}

@Serializable
data class BranchModel(
    val id: String,
    val root: Path,
    val nodes: List<NodeInfo>,
) {
    companion object {
        const val FILENAME = "branch.yml"

        @JvmStatic @StonecutterAPI
        fun load(directory: Path): Result<BranchModel> =
            load(directory.resolve(FILENAME), serializer())
    }

    internal fun save(directory: Path): Result<Unit> =
        save(directory.resolve(FILENAME), this, serializer())
}

@Serializable
data class TreeModel(
    val stonecutter: String,
    val vcs: StonecutterProject,
    val current: StonecutterProject,
    val branches: List<BranchInfo>,
    val nodes: List<NodeInfo>,
    val parameters: GlobalParameters,
) {
    companion object {
        const val FILENAME = "tree.yml"

        @JvmStatic @StonecutterAPI
        fun load(directory: Path): Result<TreeModel> =
            load(directory.resolve(FILENAME), serializer())
    }

    internal fun save(directory: Path): Result<Unit> =
        save(directory.resolve(FILENAME), this, serializer())
}