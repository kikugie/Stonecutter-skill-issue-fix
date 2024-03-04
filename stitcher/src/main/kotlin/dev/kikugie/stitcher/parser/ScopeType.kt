package dev.kikugie.stitcher.parser

import kotlinx.serialization.Serializable

/**
 * Represents the type of scope in the Stitcher program.
 *
 * Type defines how the parser should close the scope and how the transformer should modify it.
 * - [CLOSED] - comment ends with `{`. Parser will look for a comment starting with `}` to close the scope.
 * - [LINE] - comment doesn't have a key character for the ending.
 * Parser will assign the next [Block] to the scope and close it.
 * Transformer will apply the change to the next logical line.
 * (i.e. the current line or the next one if the current one has nothing after the condition)
 * - [WORD] - comment ends with `<undecided>`.
 * Parser will assign the next [Block] to the scope and close it.
 * Transformer will apply the change to the next uninterrupted string.
 * (i.e. a string that ends with a whitespace or a line break)
 */
@Serializable
enum class ScopeType {
    CLOSED,
    LINE,
    WORD
}