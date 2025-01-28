package dev.kikugie.stonecutter.controller.manager

import org.gradle.api.Project

internal val PATTERN = Regex("stonecutter[\\s.]active\\s*\\(?[\"'](\\S+)[\"']\\)?")

internal fun Project.controller() = when (buildFile.name) {
    GroovyController.filename -> GroovyController
    KotlinController.filename -> KotlinController
    else -> null
}
