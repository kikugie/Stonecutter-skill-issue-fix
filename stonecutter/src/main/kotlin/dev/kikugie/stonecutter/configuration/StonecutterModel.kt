package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.StonecutterSetup
import kotlinx.serialization.Serializable

/**
 * Data model used to pass information to the Intellij plugin.
 * It's saved to `./versions/{project}/stonecutterCache/model.bin` in CBOR format after Gradle reloads.
 *
 * @property current Metadata for this project
 * @property setup Global project setup
 * @property data Process parameters
 */
@Serializable
data class StonecutterModel(
    val current: StonecutterProject,
    val setup: StonecutterSetup,
    val data: StonecutterDataView
) {
   internal constructor(plugin: StonecutterBuild) : this(plugin.current, plugin.setup, plugin.data)
}