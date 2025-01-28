package dev.kikugie.stonecutter.controller.manager

import dev.kikugie.stonecutter.Identifier
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal interface ControllerManager {
    val filename: String
    fun createHeader(file: Path, version: Identifier)
    fun updateHeader(file: Path, version: Identifier) {
        val text = file.readText(Charsets.UTF_8)
        val new = text.replace(PATTERN) { it.value.replace(it.groupValues[1], version) }
        if (text != new) file.writeText(new, Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)
    }
}