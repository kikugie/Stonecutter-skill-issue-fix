package dev.kikugie.stonecutter.controller.manager

import dev.kikugie.stonecutter.Identifier
import java.nio.file.Path

internal interface ControllerManager {
    val filename: String
    fun createHeader(file: Path, version: Identifier)
    fun updateHeader(file: Path, version: Identifier)
}