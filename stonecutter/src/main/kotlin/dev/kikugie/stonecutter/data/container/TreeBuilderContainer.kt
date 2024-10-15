package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.controller.StonecutterController
import dev.kikugie.stonecutter.settings.builder.TreeBuilder

/**
 * Storage for the [TreeBuilder] to be passed to [StonecutterController].
 */
open class TreeBuilderContainer : ProjectContainer<TreeBuilder>()