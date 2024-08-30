package dev.kikugie.stonecutter.process

import java.nio.file.Path

internal sealed interface TransformResult {
    val file: Path

    data class Processed(override val file: Path, val str: String) : TransformResult
    data class Skipped(override val file: Path) : TransformResult
    data class Failed(override val file: Path, val error: Throwable) : TransformResult
}