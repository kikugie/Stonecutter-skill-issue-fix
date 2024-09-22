package dev.kikugie.stonecutter.process

import java.nio.file.Path

/**
 * Directories used by [FileProcessor].
 *
 * @property root The source directory
 * @property dest The output directory files will be written to if process succeeds
 * @property inputCache Cache directory for the input data
 * @property outputCache Cache directory for the output data
 */
data class DirectoryData(
    val root: Path,
    val dest: Path,
    val inputCache: Path,
    val outputCache: Path,
) {
    /**
     * Directory used by [FileProcessor] to store processed files.
     * If no errors have occurred, the contents will be copied to [dest].
     */
    val temp: Path = outputCache.resolve("temp")

    /**
     * Output cache location. If build parameters match and the file checksum is unchanged,
     * the saved output file can be directly retrieved.
     */
    val results: Path = outputCache.resolve("results")

    /**
     * Debug AST location. Used with the `debug` mode to be able to analyze AST contents.
     */
    val debug: Path = inputCache.resolve("debug")
}
