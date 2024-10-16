package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.controller.ControllerParameters
import kotlinx.serialization.Serializable

/**
 * Stores [ControllerParameters] and chiseled task names to be passed to the versioned buildscript.
 *
 * @property debug Debug flag used by the file processor
 * @property chiseled Registered chiseled tasks
 */
@Serializable
data class GlobalParameters(
    var debug: Boolean = false,
    val chiseled: MutableSet<String> = mutableSetOf()
) {
    internal fun addTask(task: String) = chiseled.add(task)
    internal fun hasChiseled(stack: Iterable<String>) = stack.any { it in chiseled }
}