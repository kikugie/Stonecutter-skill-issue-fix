package dev.kikugie.stitcher.process.cache

import dev.kikugie.stitcher.data.Scope
import dev.kikugie.stitcher.process.transformer.Container
import kotlinx.serialization.Serializable

@Serializable
data class ProcessCache(
    val container: Container,
    val ast: Scope
)