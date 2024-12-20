package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.transformer.TransformParameters
import java.nio.charset.Charset
import java.nio.file.Path

internal data class ProcessParameters(
    val dirs: DirectoryData,
    val filter: (Path) -> Boolean,
    val parameters: TransformParameters,
    val recognizers: Iterable<CommentRecognizer>,
    val statistics: ProcessStatistics,
    val charset: Charset,
    val debug: Boolean,
)
