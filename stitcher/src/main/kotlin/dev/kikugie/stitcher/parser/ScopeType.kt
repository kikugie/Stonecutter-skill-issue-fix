package dev.kikugie.stitcher.parser

import kotlinx.serialization.Serializable

@Serializable
enum class ScopeType {
    CLOSED,
    LINE,
    WORD
}