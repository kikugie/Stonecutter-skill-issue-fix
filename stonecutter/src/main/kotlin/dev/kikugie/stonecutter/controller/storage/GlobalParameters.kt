package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.TaskName

data class GlobalParameters(
    var debug: Boolean = false,
    val chiseled: MutableSet<TaskName> = mutableSetOf()
) {
    internal fun addTask(task: TaskName) = chiseled.add(task)
    internal fun hasChiseled(stack: Iterable<TaskName>) = stack.any { it in chiseled }
}