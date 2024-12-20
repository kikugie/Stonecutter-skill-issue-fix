package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.controller.manager.controller
import dev.kikugie.stonecutter.settings.StonecutterSettings
import dev.kikugie.stonecutter.data.container.ConfigurationService
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider

internal open class StonecutterPlugin : Plugin<ExtensionAware> {
    internal companion object {
        lateinit var SERVICE: Provider<ConfigurationService>

        private fun Gradle.createConfigurationService() = sharedServices
            .registerIfAbsent(ConfigurationService.NAME, ConfigurationService::class.java)
            .also { SERVICE = it }
    }

    override fun apply(target: ExtensionAware) {
        if (target is Settings) target.gradle.createConfigurationService()
        val type = if (target is Settings)
            StonecutterSettings::class
        else if (target is Project)
            if (target.controller() == null) StonecutterBuild::class
            else StonecutterController::class
        else error("The plugin may only be applied to settings and projects")
        target.extensions.create("stonecutter", type.java, target)
    }
}