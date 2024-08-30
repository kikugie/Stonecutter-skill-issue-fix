package dev.kikugie.stonecutter.process

sealed interface ProcessResult {
    object FilterExcluded : ProcessResult
    object CacheMatches: ProcessResult
    object NewMatches : ProcessResult

    data class ResultCached(val content: String) : ProcessResult
    data class NewProcessed(val content: String) : ProcessResult
}