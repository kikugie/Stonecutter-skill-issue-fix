package dev.kikugie.stonecutter.process

import java.nio.file.Path

internal data class DirectoryData(
    val input: Path,
    val output: Path,
    val debug: Path,
    val temp: Path,
)
