package dev.kikugie.stonecutter.data.parameters

import dev.kikugie.semver.Version
import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.build.BuildAbstraction
import kotlinx.serialization.Serializable

/**
 * Represents the build parameters used by the file processor.
 *
 * @property constants Constant values set by [BuildAbstraction.const]
 * @property swaps Swap replacements set by [BuildAbstraction.swap]
 * @property dependencies Dependency versions set by [BuildAbstraction.dependency]
 */
@Serializable
data class BuildParameters(
    val constants: MutableMap<Identifier, Boolean> = mutableMapOf(),
    val swaps: MutableMap<Identifier, String> = mutableMapOf(),
    val dependencies: MutableMap<Identifier, Version> = mutableMapOf(),
    val extensions: MutableSet<String> = mutableSetOf("java", "kt", "kts", "groovy", "gradle", "scala", "sc", "json5", "hjson"),
    val exclusions: MutableSet<String> = mutableSetOf()
)