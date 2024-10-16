package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.controller.storage.ProjectTree
import dev.kikugie.stonecutter.controller.StonecutterController

/**
 * Saves the finished [ProjectTree] in [StonecutterController] to be available with
 * ```
 * project.gradle.extensions.getByType<ProjectTreeContainer>()
 * ```
 */
open class ProjectTreeContainer : ProjectContainer<ProjectTree>()