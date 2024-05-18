package dev.kikugie.stonecutter.util

import org.gradle.api.Project
import org.gradle.api.initialization.Settings

val Settings.filename
    get() = if (settingsDir.resolve("settings.gradle.kts").exists()) "settings.gradle.kts" else "settings.gradle"

val Project.buildDirectory
    get() = layout.buildDirectory.asFile.get()