package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.controller.storage.ProjectNode
import dev.kikugie.stonecutter.data.container.ProjectTreeContainer
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.jetbrains.annotations.ApiStatus

/**
 * Chiseled tasks allow Stonecutter to run specified jobs for all versions.
 */
@Suppress("LeakingThis", "unused", "DEPRECATION")
abstract class ChiseledTask : DefaultTask() {
    private val tree = requireNotNull(project.gradle.extensions.getByType(ProjectTreeContainer::class.java)[project]) {
        "Chiseled task registered in a non-Stonecutter project"
    }
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    /**
     * Project tree nodes used by the task. Use [nodes] or [versions] function.
     */
    @get:Input
    @get:ApiStatus.Internal
    abstract val nodes: ListProperty<ProjectNode>

    /**
     * Versions used by the task.
     */
    @get:Input
    @get:ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @get:Deprecated("Must assign nodes. Use versions() or nodes() to filter the tree directly.")
    abstract val versions: ListProperty<StonecutterProject>

    /**
     * Assigns nodes to execute this task on from the tree.
     * **Accessing project properties in Groovy at this stage might be inaccurate.**
     *
     * @param selector Node filtering function
     * @see ProjectNode
     */
    fun nodes(selector: (ProjectNode) -> Boolean) {
        nodes.set(tree.nodes.filter(selector))
    }

    /**
     * Assigns nodes to execute this task on from the tree.
     *
     * @param selector Node filtering function
     * @see NodeFilter
     * @see StonecutterProject
     */
    fun versions(selector: NodeFilter) {
        nodes.set(tree.nodes.filter { selector(it.branch.id, it.metadata) })
    }

    init {
        dependsOn(setupTask)
        nodes.convention(tree.nodes)
        versions.convention(emptyList())
    }

    /**
     * Provides the delegate task for this chiseled task instance.
     * Must be called **after** specifying nodes.
     *
     * @param name The name of the task to be executed
     */
    fun ofTask(name: String) = nodes.get().onEach {
        versions.get().run {
            if (isNotEmpty() && metadata !in this) return@onEach
        }
        tasks.findByName(name)?.let { task ->
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }

    /**
     * Filtering function for nodes, independent from the underlying Gradle project.
     */
    fun interface NodeFilter {
        /**
         * Invokes the filter function for a given project branch and version.
         *
         * @param branch The name of the project branch
         * @param version The Stonecutter project version
         * @return A boolean indicating whether the filter criteria are met
         */
        operator fun invoke(branch: ProjectName, version: StonecutterProject): Boolean
    }
}