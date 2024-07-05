package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterSetup
import kotlinx.serialization.Serializable

@Serializable
data class StonecutterModel(val setup: StonecutterSetup, val data: StonecutterDataView) {
    constructor(plugin: StonecutterBuild) : this(plugin.setup, plugin.data)
}