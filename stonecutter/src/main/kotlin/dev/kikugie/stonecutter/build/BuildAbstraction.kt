package dev.kikugie.stonecutter.build

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.container.ConfigurationService.Companion.of
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.controller.ControllerAbstraction
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Contains logic for the versioned buildscript, which is separated to allow
 * global configuration by [ControllerAbstraction.parameters].
 * @property hierarchy Path of the corresponding project
 */
abstract class BuildAbstraction(protected val hierarchy: ProjectHierarchy) :
    SwapVariants, ConstantVariants, DependencyVariants, FilterVariants, ReplacementVariants {
    protected val data: BuildParameters = checkNotNull(StonecutterPlugin.SERVICE.of(hierarchy).build) {
        "Stonecutter build parameters not found"
    }

    override fun swap(identifier: Identifier, replacement: String) {
        data.swaps[identifier.validateId()] = replacement
    }

    override fun const(identifier: Identifier, value: Boolean) {
        data.constants[identifier.validateId()] = value
    }

    override fun dependency(identifier: Identifier, version: SemanticVersion) {
        data.dependencies[identifier.validateId()] = version.validateVersion()
    }

    override fun replacement(direction: Boolean, source: String, target: String) {
        if (direction) data.replacements.basic(source, target)
        else data.replacements.basic(target, source)
    }

    @StonecutterDelicate
    override fun replacement(
        direction: Boolean,
        sourcePattern: String,
        targetValue: String,
        targetPattern: String,
        sourceValue: String
    ) {
        if (direction) data.replacements.regex(sourcePattern, targetValue)
        else data.replacements.regex(targetPattern, sourceValue)
    }

    override fun allowExtensions(extensions: Iterable<String>) {
        data.extensions += extensions
    }

    override fun overrideExtensions(extensions: Iterable<String>) =
        data.extensions.clear() then allowExtensions(extensions)

    override fun excludeFiles(files: Iterable<String>) = files.forEach {
        require(it.startsWith("src/")) { "File path must start with 'src/': $it" }
        Path(it).normalize().invariantSeparatorsPathString.let(data.exclusions::add)
    }

    internal fun from(other: BuildAbstraction): Unit = with(data) {
        swaps.putAll(other.data.swaps)
        constants.putAll(other.data.constants)
        dependencies.putAll(other.data.dependencies)
        extensions.addAll(other.data.extensions)
        exclusions.addAll(other.data.exclusions)
    }
}