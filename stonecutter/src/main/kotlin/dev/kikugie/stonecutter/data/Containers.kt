package dev.kikugie.stonecutter.data

import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.storage.GlobalParameters
import dev.kikugie.stonecutter.controller.storage.ProjectTree
import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.settings.builder.TreeBuilder

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

/**
 * Storage for the [TreeBuilder] to be passed to [StonecutterController].
 */
open class TreeBuilderContainer : ProjectContainer<TreeBuilder>()