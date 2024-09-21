package dev.kikugie.stonecutter.controller.manager

import dev.kikugie.stonecutter.ProjectName
import java.nio.file.Path

internal interface ControllerManager {
    val filename: String
    fun createHeader(file: Path, version: ProjectName)
    fun updateHeader(file: Path, version: ProjectName)
}