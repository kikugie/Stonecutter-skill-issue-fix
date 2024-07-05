package dev.kikugie.stonecutter.process

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

internal data class FileFilter(
    private val excludedExtensions: Set<String>,
    private val excludedPaths: Set<Path>
) {
    fun shouldProcess(path: Path): Boolean = when {
        path.extension in excludedExtensions -> false
        path in excludedPaths -> false
        excludedPaths.any { it.isDirectory() && path.startsWith(it) } -> false
        else -> true
    }
}
