package dev.kikugie.stonecutter.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.useLines
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

private const val KEY = "/* [SC] DO NOT EDIT */"

val Project.controller
    get() = when (buildFile.name) {
        GroovyController.filename -> GroovyController
        KotlinController.filename -> KotlinController
        else -> null
    }

interface ControllerManager {
    val filename: String
    fun createHeader(file: Path, version: ProjectName)
    fun updateHeader(file: Path, version: ProjectName)
}

object GroovyController : ControllerManager {
    override val filename = "stonecutter.gradle"
    override fun createHeader(file: Path, version: ProjectName) {
        file.writeText(
            """
                plugins.apply "dev.kikugie.stonecutter"
                stonecutter.active "$version" $KEY
            """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun updateHeader(file: Path, version: ProjectName) {
        var newLines = emptyList<String>()
        var corrupted = true
        file.useLines {
            newLines = it.map { line ->
                if (line.trim().endsWith(KEY)) {
                    corrupted = false
                    "stonecutter.active \"$version\" $KEY"
                } else line
            }.toList()
        }
        if (newLines.isEmpty()) throw GradleException("[Stonecutter] Empty stonecutter.gradle file")
        if (corrupted) throw GradleException("[Stonecutter] Invalid stonecutter.gradle script")
        file.writeLines(newLines, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}

object KotlinController : ControllerManager {
    override val filename = "stonecutter.gradle.kts"
    override fun createHeader(file: Path, version: ProjectName) {
        file.writeText(
            """
                plugins {
                    id("dev.kikugie.stonecutter")
                }
                stonecutter active "$version" $KEY
            """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun updateHeader(file: Path, version: ProjectName) {
        var newLines = emptyList<String>()
        var corrupted = true
        file.useLines {
            newLines = it.map { line ->
                if (line.trim().endsWith(KEY)) {
                    corrupted = false
                    "stonecutter active \"$version\" $KEY"
                } else line
            }.toList()
        }
        if (newLines.isEmpty()) throw GradleException("[Stonecutter] Empty stonecutter.gradle.kts file")
        if (corrupted) throw GradleException("[Stonecutter] Invalid stonecutter.gradle.kts script")
        file.writeLines(newLines, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}