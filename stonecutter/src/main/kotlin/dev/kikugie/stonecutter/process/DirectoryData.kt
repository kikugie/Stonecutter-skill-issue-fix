package dev.kikugie.stonecutter.process

import java.nio.file.Path

data class DirectoryData(
    val root: Path,
    val dest: Path,
    val inputCache: Path,
    val outputCache: Path,
) {
    val temp: Path = outputCache.resolve("temp")
    val results: Path = outputCache.resolve("results")
    val asts: Path = inputCache.resolve("asts")
    val debug: Path = inputCache.resolve("debug")
}
