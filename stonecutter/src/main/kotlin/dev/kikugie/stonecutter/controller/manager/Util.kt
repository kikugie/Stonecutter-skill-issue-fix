package dev.kikugie.stonecutter.controller.manager

import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.useLines
import kotlin.io.path.writeLines

internal const val KEY = "/* [SC] DO NOT EDIT */"

internal fun Project.controller() = when (buildFile.name) {
    GroovyController.filename -> GroovyController
    KotlinController.filename -> KotlinController
    else -> null
}

internal fun updateFileWithKey(file: Path, filename: String, key: String) {
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
    check(newLines.isNotEmpty()) { emptyFile(filename) }
    check(!corrupted) { invalidScript(filename, key) }
    file.writeLines(newLines, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

internal fun emptyFile(name: String) = """
    $name is empty. This might have been caused by a user error.
    If this is intentional, delete the file to make Stonecutter regenerate it.
    """.trimIndent()

internal fun invalidScript(name: String, missing: String) = """
    Couldn't find active version specification in $name.
    Add `$missing` or delete the file to make Stonecutter regenerate it
    """.trimIndent()
