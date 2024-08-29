package dev.kikugie.experimentalstonecutter.data

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dev.kikugie.semver.VersionParser
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.experimentalstonecutter.buildDirectory
import org.gradle.api.Project

internal val Project.buildDirectoryFile
    get() = layout.buildDirectory.asFile.get()

internal val Project.buildDirectoryPath
    get() = layout.buildDirectory.asFile.get().toPath()

internal val Project.stonecutterCacheFile
    get() = buildDirectory.resolve("stonecutter-cache")

internal val Project.stonecutterCachePath
    get() = stonecutterCacheFile.toPath()

internal val MODEL_NAME = "model.yml"
internal val TREE_NAME = "tree.yml"

val YAML = Yaml(
    configuration = YamlConfiguration(
        strictMode = false
    )
)

inline fun <T : Any> runIgnoring(action: () -> T): T? = runCatching(action).getOrNull()

fun StitcherParameters.toParams(version: String, key: String = "minecraft"): TransformParameters {
    val deps = dependencies.toMutableMap()
    deps.getOrElse(key) { VersionParser.parseLenient(version)}.let {
        deps[key] = it
        deps[""] = it
    }
    return TransformParameters(swaps, constants, deps)
}