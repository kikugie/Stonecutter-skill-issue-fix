package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.controller.storage.GlobalParameters

/**
 * Saves the global parameters defined in [StonecutterController] to be available in [StonecutterBuild] with
 * ```
 * project.gradle.extensions.getByType<ProjectTreeContainer>()
 * ```
 */
open class ProjectParameterContainer : ProjectContainer<GlobalParameters>()