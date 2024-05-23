package dev.kikugie.version.parse

import dev.kikugie.version.exception.VersionParsingException

interface Parser<T> {
    @Throws(VersionParsingException::class)
    fun parse(input: CharSequence): T
}