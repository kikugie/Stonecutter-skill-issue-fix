package dev.kikugie.stonecutter.configuration

import org.gradle.api.Project

internal val Project.buildDirectory
    get() = layout.buildDirectory.asFile.get()

internal val Project.stonecutterCacheDir
    get() = buildDirectory.resolve("stonecutter-cache")

internal val Project.stonecutterCachePath
    get() = stonecutterCacheDir.toPath()
