package dev.kikugie.stonecutter

import dev.kikugie.stonecutter.controller.storage.ProjectNode
import dev.kikugie.stonecutter.data.container.ProjectTreeContainer
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.jetbrains.annotations.ApiStatus

// link: wiki-chisel
/**
 * Wrapper task Stonecutter uses to configure the delegate provided by [ofTask] to run on all [nodes].
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#chiseled-tasks">Wiki page</a>
 */
@Suppress("LeakingThis", "unused", "DEPRECATION")
abstract class ChiseledTask : DefaultTask() {
    private val tree = requireNotNull(project.gradle.extensions.getByType(ProjectTreeContainer::class.java)[project]) {
        "Chiseled task registered in a non-Stonecutter project"
    }
    private val setupTask: Task = project.tasks.getByName("chiseledStonecutter")

    /**
     * Project tree nodes used by the task. Can be assigned directly,
     * but using [versions] or [nodes] function is preferred.
     */
    @get:Input
    @get:ApiStatus.Internal
    abstract val nodes: ListProperty<ProjectNode>

    /**
     * Version filter used by this task. Can be assigned directly,
     * but using [versions] function is preferred.
     */
    @get:Input
    @get:ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @get:Deprecated("Must assign nodes. Use versions() or nodes() to filter the tree directly.")
    abstract val versions: ListProperty<StonecutterProject>

    /**
     * Filters the nodes by the project instance.
     * **Accessing project properties in Groovy at this stage might be inaccurate.**
     */
    fun nodes(selector: (ProjectNode) -> Boolean) {
        nodes.set(tree.nodes.filter(selector))
    }

    /**
     * Filters the nodes by the branch name and the project metadata.
     * If you're not using multi-platform builds, the branch parameter can be ignored.
     *
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

    /**Filtering function for nodes, independent from the underlying Gradle project.*/
    fun interface NodeFilter {
        /**Invokes the filter function for the given project [branch] and [version].*/
        operator fun invoke(branch: Identifier, version: StonecutterProject): Boolean
    }
}