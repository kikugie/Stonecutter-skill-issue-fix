package dev.kikugie.stonecutter.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

@Suppress("LeakingThis", "unused")
abstract class ChiseledTask : DefaultTask() {
    private val setup = project.gradle.extensions.getByType(ProjectSetup.SetupContainer::class.java)[project]
        ?: throw GradleException("Project ${project.path} is not registered in Stonecutter")
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    @get:Input
    abstract val versions: ListProperty<String>

    init {
        dependsOn(setupTask)
        versions.convention(setup.versions)
    }

    fun ofTask(name: String) {
        versions.get().forEach {
            val task = project.project(it).tasks.getByName(name)
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }
}