package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.TaskName
import dev.kikugie.stonecutter.controller.ControllerParameters

/**
 * Stores [ControllerParameters] and chiseled task names to be passed to the versioned buildscript.
 *
 * @property debug Debug flag used by the file processor
 * @property chiseled Registered chiseled tasks
 */
data class GlobalParameters(
    var debug: Boolean = false,
    val chiseled: MutableSet<TaskName> = mutableSetOf()
) {
    internal fun addTask(task: TaskName) = chiseled.add(task)
    internal fun hasChiseled(stack: Iterable<TaskName>) = stack.any { it in chiseled }
}