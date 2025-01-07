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
public abstract class BuildAbstraction(protected val hierarchy: ProjectHierarchy) :
    SwapVariants, ConstantVariants, DependencyVariants, FilterVariants, ReplacementVariants {
    protected val data: BuildParameters = checkNotNull(StonecutterPlugin.SERVICE.of(hierarchy).build) {
        "Stonecutter build parameters not found for $hierarchy. Present keys:\n%s"
            .format(StonecutterPlugin.SERVICE().parameters.buildParameters.keysToString())
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

    override fun replacement(phase: ReplacementPhase, direction: Boolean, source: String, target: String) {
        if (direction) data.replacements.string(phase, source, target)
        else data.replacements.string(phase, target, source)
    }

    @StonecutterDelicate
    override fun replacement(
        phase: ReplacementPhase,
        direction: Boolean,
        sourcePattern: String,
        targetValue: String,
        targetPattern: String,
        sourceValue: String
    ) {
        if (direction) data.replacements.regex(phase, sourcePattern, targetValue)
        else data.replacements.regex(phase, targetPattern, sourceValue)
    }

    override fun allowExtensions(extensions: Iterable<String>) {
        data.extensions += extensions
    }

    override fun overrideExtensions(extensions: Iterable<String>): Unit =
        data.extensions.clear() then allowExtensions(extensions)

    override fun excludeFiles(files: Iterable<String>): Unit = files.forEach {
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