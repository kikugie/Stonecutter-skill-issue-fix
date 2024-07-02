package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.configuration.StonecutterModelBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import javax.inject.Inject

internal open class StonecutterPlugin @Inject constructor(
    private val registry: ToolingModelBuilderRegistry
) : Plugin<ExtensionAware> {
    override fun apply(target: ExtensionAware) {
        val type = if (target is Settings)
            StonecutterSettings::class
        else if (target is Project)
            if (target.controller() != null) StonecutterController::class
            else {
                registry.register(StonecutterModelBuilder)
                StonecutterBuild::class
            }
        else throw StonecutterGradleException("The plugin may only be applied to settings and projects")
        target.extensions.create("stonecutter", type.java, target)
    }
}