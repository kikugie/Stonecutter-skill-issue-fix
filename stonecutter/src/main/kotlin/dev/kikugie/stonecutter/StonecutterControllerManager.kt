package dev.kikugie.stonecutter

import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.useLines
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

private const val KEY = "/* [SC] DO NOT EDIT */"

internal fun Project.controller() = when (buildFile.name) {
    GroovyController.filename -> GroovyController
    KotlinController.filename -> KotlinController
    else -> null
}

internal interface ControllerManager {
    val filename: String
    fun createHeader(file: Path, version: ProjectName)
    fun updateHeader(file: Path, version: ProjectName)
}

internal object GroovyController : ControllerManager {
    override val filename = "stonecutter.gradle"
    override fun createHeader(file: Path, version: ProjectName) {
        file.writeText(
            """
            plugins.apply "dev.kikugie.stonecutter"
            stonecutter.active "$version" $KEY
            
            stonecutter.registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
                setGroup "project"
                ofTask "build"
            }
            """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun updateHeader(file: Path, version: ProjectName) =
        updateFileWithKey(file, filename, "stonecutter.active \"$version\" $KEY")
}

internal object KotlinController : ControllerManager {
    override val filename = "stonecutter.gradle.kts"
    override fun createHeader(file: Path, version: ProjectName) {
        file.writeText(
            """
            plugins {
                id("dev.kikugie.stonecutter")
            }
            stonecutter active "$version" $KEY
            
            stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
                group = "project"
                ofTask("build")
            }
            """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun updateHeader(file: Path, version: ProjectName) =
        updateFileWithKey(file, filename, "stonecutter active \"$version\" $KEY")
}

private fun updateFileWithKey(file: Path, filename: String, key: String) {
    var newLines = emptyList<String>()
    var corrupted = true
    file.useLines {
        newLines = it.map { line ->
            if (line.trim().endsWith(KEY)) {
                corrupted = false
                key
            } else line
        }.toList()
    }
    if (newLines.isEmpty()) throw emptyFile(filename)
    if (corrupted) throw invalidScript(filename, key)
    file.writeLines(newLines, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

private fun emptyFile(name: String): Throwable =
    StonecutterGradleException(
        """
        $name is empty. This might have been caused by a user error.
        If this is intentional, delete the file to make Stonecutter regenerate it.
        """.trimIndent()
    )

private fun invalidScript(name: String, missing: String): Throwable =
    StonecutterGradleException(
        """
        Couldn't find active version specification in $name.
        Add `$missing` or delete the file to make Stonecutter regenerate it
        """.trimIndent()
    )

internal val Project.buildDirectory
    get() = layout.buildDirectory.asFile.get()