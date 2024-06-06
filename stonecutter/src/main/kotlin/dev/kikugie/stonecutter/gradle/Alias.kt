@file:Suppress("unused")

package dev.kikugie.stonecutter.gradle

import dev.kikugie.stonecutter.StonecutterSettings

/**
 * Used to avoid import errors when migrating from 0.3 to 0.4.
 */
@Deprecated("Moved to the new package", ReplaceWith(expression = "StonecutterSettings", imports = ["dev.kikugie.stonecutter.StonecutterSettings"]))
typealias StonecutterSettings = StonecutterSettings