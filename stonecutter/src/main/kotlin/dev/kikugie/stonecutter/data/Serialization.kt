package dev.kikugie.stonecutter.data

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

internal val MODEL_NAME = "model.yml"
internal val TREE_NAME = "tree.yml"

val YAML = Yaml(
    configuration = YamlConfiguration(
        strictMode = false
    )
)

inline fun <T : Any> runIgnoring(action: () -> T): T? = runCatching(action).getOrNull()