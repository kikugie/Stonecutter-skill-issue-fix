package dev.kikugie.stonecutter.process

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

/**
 * Represents a file filter used to determine whether a file should be processed or not based on its extension and path.
 *
 * @property excludedExtensions A set of file extensions to be excluded from processing.
 * @property excludedPaths A set of file paths to be excluded from processing.
 */
data class FileFilter(
    private val excludedExtensions: Set<String>,
    private val excludedPaths: Set<Path>
) : (Path) -> Boolean {
    /**
     * Determines if the given path should be processed.
     */
    override fun invoke(path: Path): Boolean = when {
        path.extension in excludedExtensions -> false
        path in excludedPaths -> false
        excludedPaths.any { it.isDirectory() && path.startsWith(it) } -> false
        else -> true
    }
}
