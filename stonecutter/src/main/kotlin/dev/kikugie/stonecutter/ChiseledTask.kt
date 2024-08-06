package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.configuration.StonecutterSetup
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

/**
 * Chiseled tasks allow Stonecutter to run specified jobs for all versions.
 */
@Suppress("LeakingThis", "unused")
abstract class ChiseledTask : DefaultTask() {
    private val setup = project.gradle.extensions.getByType(StonecutterSetup.Container::class.java)[project]!!
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    /**
     * Specifies projects this task runs on.
     * Use in combination with `stonecutter.versions.filter { }`.
     */
    @get:Input
    abstract val versions: ListProperty<StonecutterProject>

    init {
        dependsOn(setupTask)
        versions.convention(setup.versions)
    }

    /**
     * Specifies which task this delegates to.
     *
     * @param name delegate task
     */
    fun ofTask(name: String) {
        versions.get().forEach {
            val task = project.project(it.project).tasks.getByName(name)
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }
}