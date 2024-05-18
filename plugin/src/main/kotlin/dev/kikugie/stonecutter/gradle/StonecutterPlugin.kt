package dev.kikugie.stonecutter.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

internal open class StonecutterPlugin : Plugin<ExtensionAware> {
    override fun apply(target: ExtensionAware) {
        val type = if (target is Settings)
            StonecutterSettings::class
        else if (target is Project)
            if (target.controller() == null) StonecutterBuild::class
            else StonecutterController::class
        else throw StonecutterGradleException("The plugin may only be applied to settings and projects")
        target.extensions.create("stonecutter", type.java, target)
    }
}