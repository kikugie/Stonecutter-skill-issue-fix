package dev.kikugie.stonecutter.process

import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.transformer.TransformParameters
import java.nio.charset.Charset

internal interface ProcessParameters {
    val directory: DirectoryData
    val filter: FileFilter
    val parameters: TransformParameters
    val statistics: ProcessStatistics
    val recognizers: Iterable<CommentRecognizer>
    val charset: Charset
    val debug: Boolean
    val cache: Boolean
}