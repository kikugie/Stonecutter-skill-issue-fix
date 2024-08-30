package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.BuildConfiguration
import dev.kikugie.stonecutter.data.StitcherParameters

/**
 * Stores parameters configured in [StonecutterController.configureAll].
 *
 * @property branch Currently processed branch
 * @property version Currently processed version.
 * **May not exist in the given branch**, but you should still provide the same set of parameters.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ParameterHolder(
    val branch: ProjectBranch,
    val version: StonecutterProject
) : BuildConfiguration(branch) {
    init {
        data = StitcherParameters()
    }
}