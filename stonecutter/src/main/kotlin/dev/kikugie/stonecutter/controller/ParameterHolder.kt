package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.StonecutterAPI
import dev.kikugie.stonecutter.StonecutterUtility
import dev.kikugie.stonecutter.build.BuildAbstraction
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.tree.LightBranch
import dev.kikugie.stonecutter.data.tree.LightNode

// link: wiki-controller-params
/**
 * Stores parameters configured in [StonecutterController.parameters].
 *
 * @property branch Currently processed branch
 * @property metadata Currently processed version.
 * **May not exist in the given branch**, but you should still provide the same set of parameters
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#global-parameters">Wiki page</a>
 */
public class ParameterHolder(
    @StonecutterAPI public val branch: LightBranch,
    @StonecutterAPI public val metadata: StonecutterProject
) : BuildAbstraction(branch.hierarchy + metadata.project), StonecutterUtility {
    /**
     * Project node matching [metadata] on [branch].
     * May be `null` when branches have different sets of versions.
     */
    @StonecutterAPI public val node: LightNode? = branch[metadata.project]
}