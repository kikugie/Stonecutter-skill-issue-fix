package dev.kikugie.stonecutter.configuration

import dev.kikugie.experimentalstonecutter.build.StonecutterData
import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterController
import dev.kikugie.stonecutter.process.*
import org.gradle.api.Project
import java.nio.file.Path

internal val Project.buildDirectory
    get() = layout.buildDirectory.asFile.get()

internal val Project.stonecutterCacheDir
    get() = buildDirectory.resolve("stonecutter-cache")

internal val Project.stonecutterCachePath
    get() = stonecutterCacheDir.toPath()

internal fun writeBuildModel(plugin: StonecutterBuild) = runIgnoring {
    plugin.project.stonecutterCacheDir.resolve("model.yml").toPath().encodeYaml(plugin.data)
}

fun readBuildModel(file: Path) = runCatching {
    file.decodeYaml<StonecutterData>()
}

internal fun writeControllerModel(plugin: StonecutterController) = runIgnoring {
    plugin.root.stonecutterCacheDir.resolve("model.yml").toPath().encodeYaml(plugin.setup)
}

fun readControllerModel(file: Path) = runCatching {
    file.decodeYaml<StonecutterSetup>()
}