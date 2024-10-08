package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.data.ProjectTreeContainer
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

/**
 * Chiseled tasks allow Stonecutter to run specified jobs for all versions.
 */
@Suppress("LeakingThis", "unused")
abstract class ChiseledTask : DefaultTask() {
    private val tree = requireNotNull(project.gradle.extensions.getByType(ProjectTreeContainer::class.java)[project]) {
        "Chiseled task registered in a non-Stonecutter project"
    }
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    /**
     * Specifies projects this task runs on.
     * Use in combination with `stonecutter.versions.filter { }`.
     */
    @get:Input
    abstract val versions: ListProperty<StonecutterProject>

    init {
        dependsOn(setupTask)
        versions.convention(tree.versions)
    }

    /**
     * Specifies which task this delegates to.
     *
     * @param name delegate task
     */
    fun ofTask(name: String) {
        val versions = versions.get().toSet()
        tree.nodes.filter { it.metadata in versions }.forEach {
            val task = it.tasks.getByName(name)
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }
}