package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.StonecutterAPI
import dev.kikugie.stonecutter.StonecutterPlugin
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.hierarchy
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.locate
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.container.ConfigurationService.Companion.of
import dev.kikugie.stonecutter.data.tree.NodePrototype
import org.gradle.api.DefaultTask
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
public abstract class ChiseledTask : DefaultTask() {
    @Transient
    private val tree = StonecutterPlugin.SERVICE.of(project.hierarchy).tree
        ?: error("Chiseled task registered in a non-Stonecutter project")

    @Transient
    private val setupTask: String = project.tasks.getByName("chiseledStonecutter").path

    /**
     * Project tree nodes used by the task. Can be assigned directly,
     * but using [versions] or [nodes] function is preferred.
     */
    @get:Input
    @get:ApiStatus.Internal
    public abstract val nodes: ListProperty<NodePrototype>

    /**
     * Version filter used by this task. Can be assigned directly,
     * but using [versions] function is preferred.
     */
    @get:Input
    @get:ApiStatus.ScheduledForRemoval(inVersion = "0.6")
    @get:Deprecated("Must assign nodes. Use versions() or nodes() to filter the tree directly.")
    public abstract val versions: ListProperty<StonecutterProject>

    /**
     * Filters the nodes by the project instance.
     * **Accessing project properties in Groovy at this stage might be inaccurate.**
     */
    @StonecutterAPI public fun nodes(selector: (NodePrototype) -> Boolean) {
        tree.nodes.filter(selector).let(nodes::set)
    }

    /**
     * Filters the nodes by the branch name and the project metadata.
     * If you're not using multi-platform builds, the branch parameter can be ignored.
     *
     * @see StonecutterProject
     */
    @StonecutterAPI public fun versions(selector: NodeFilter) {
        tree.nodes.filter { selector(it.branch.id, it.metadata) }.let(nodes::set)
    }

    init {
        dependsOn(setupTask)
        nodes.convention(tree.nodes)
        versions.convention(emptyList())
    }

    /**
     * Provides the delegate task for this chiseled task instance.
     * Must be called **after** specifying [nodes].
     *
     * @param name The name of the task to be executed
     */
    @StonecutterAPI public fun ofTask(name: String): Unit = nodes.get().forEach {
        versions.get().run {
            if (isNotEmpty() && it.metadata !in this) return@forEach
        }
        // We can reference the project here, even though it's transient,
        // because this is called in the configuration stage.
        project.locate(it.hierarchy).tasks.findByName(name)?.let { task ->
            finalizedBy(task)
            task.mustRunAfter(setupTask)
        }
    }

    /**Filtering function for nodes, independent from the underlying Gradle project.*/
    public fun interface NodeFilter {
        /**Invokes the filter function for the given project [branch] and [version].*/
        public operator fun invoke(branch: Identifier, version: StonecutterProject): Boolean
    }
}