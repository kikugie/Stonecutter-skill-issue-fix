package dev.kikugie.stonecutter.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

@Suppress("LeakingThis", "unused")
abstract class ChiseledTask : DefaultTask() {
    private val setup = project.gradle.extensions.getByType(StonecutterConfiguration.Container::class.java)[project]!!
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    @get:Input
    abstract val versions: ListProperty<StonecutterProject>

    init {
        dependsOn(setupTask)
        versions.convention(setup.versions)
    }

    fun ofTask(name: String) {
        versions.get().forEach {
            val task = project.project(it.project).tasks.getByName(name)
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }
}