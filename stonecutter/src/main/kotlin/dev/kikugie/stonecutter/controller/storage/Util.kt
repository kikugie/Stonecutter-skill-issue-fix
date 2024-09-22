package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.data.ProjectContainer

internal operator fun ProjectBranch?.get(project: ProjectName) = this?.nodes?.get(project)

/**
 * Saves the finished [ProjectTree] in [StonecutterController] to be available with
 * ```
 * project.gradle.extensions.getByType<ProjectTreeContainer>()
 * ```
 */
open class ProjectTreeContainer : ProjectContainer<ProjectTree>()

/**
 * Saves the global parameters defined in [StonecutterController] to be available in [StonecutterBuild] with
 * ```
 * project.gradle.extensions.getByType<ProjectTreeContainer>()
 * ```
 */
open class ProjectParameterContainer : ProjectContainer<GlobalParameters>()