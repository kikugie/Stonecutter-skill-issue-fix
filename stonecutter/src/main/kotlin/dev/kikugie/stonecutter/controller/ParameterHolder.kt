package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.build.BuildConfiguration

/**
 * Stores parameters configured in [StonecutterController.parameters].
 *
 * @property branch Currently processed branch
 * @property version Currently processed version.
 * **May not exist in the given branch**, but you should still provide the same set of parameters.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ParameterHolder(
    val branch: ProjectBranch,
    val version: StonecutterProject
) : BuildConfiguration(branch)