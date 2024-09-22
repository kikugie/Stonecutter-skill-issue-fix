package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.StonecutterUtility
import dev.kikugie.stonecutter.build.BuildConfiguration
import dev.kikugie.stonecutter.controller.storage.ProjectBranch
import dev.kikugie.stonecutter.controller.storage.ProjectNode

/**
 * Stores parameters configured in [StonecutterController.parameters].
 *
 * @property branch Currently processed branch
 * @property metadata Currently processed version.
 * **May not exist in the given branch**, but you should still provide the same set of parameters
 */
class ParameterHolder(
    val branch: ProjectBranch,
    val metadata: StonecutterProject
) : BuildConfiguration(branch), StonecutterUtility {
    /**
     * Project node matching [project] on [branch].
     * May be `null` when branches have different sets of versions.
     */
    val node: ProjectNode? = branch[metadata.project]
}