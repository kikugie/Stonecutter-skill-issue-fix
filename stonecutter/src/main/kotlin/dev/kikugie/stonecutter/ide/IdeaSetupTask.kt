package dev.kikugie.stonecutter.ide

import dev.kikugie.stonecutter.StonecutterPlugin
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.tree.TreePrototype
import dev.kikugie.stonecutter.invoke
import dev.kikugie.stonecutter.readResource
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.StandardOpenOption
import kotlin.io.path.deleteExisting
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

@Suppress("LeakingThis")
internal abstract class IdeaSetupTask : DefaultTask() {
    companion object {
        val TEMPLATE: Result<String> by lazy { readResource("idea_config.xml") }
    }

    @get:Input
    abstract val trees: ListProperty<TreePrototype<*>>

    private val folder = project.rootDir.absoluteFile.resolve(".idea/runConfigurations").toPath()

    init {
        group = "ide"
        trees.convention(StonecutterPlugin.SERVICE().parameters.projectTrees().values)
    }

    @TaskAction
    fun run() {
        if (TEMPLATE.isFailure) return logger.error("Failed to read template configuration file", TEMPLATE.exceptionOrNull())

        val files = mutableSetOf<String>()
        for (tree in trees()) files.addAll(configureTree(tree))

        for (file in folder.listDirectoryEntries()) if (file.fileName.toString() !in files) kotlin.runCatching { file.deleteExisting() }
            .onFailure { logger.error("Failed to delete configuration file $file", it) }
    }

    private fun configureTree(tree: TreePrototype<*>) = buildList {
        writeConfiguration(tree.hierarchy, "Reset active", "\"Reset active project\"")
        writeConfiguration(tree.hierarchy, "Refresh active", "\"Refresh active project\"")
        for ((name, _) in tree.versions)
            writeConfiguration(tree.hierarchy, "Switch to $name", "\"Set active project to $name\"")
    }

    private fun MutableList<String>.writeConfiguration(project: ProjectHierarchy, name: String, task: String = name) {
        val xml = TEMPLATE.getOrThrow()
            .replaceChecked("%FOLDER_NAME%", "Stonecutter${project.orBlank()}")
            .replaceChecked("%ENTRY_NAME%", name)
            .replaceChecked("%TASK_NAME%", "${project.orBlank()}:$task")
        val filename = "Stonecutter${project.toString().replace(':', '_')}_${name.replace(' ', '_')}.xml"
        kotlin.runCatching {
            folder.resolve(filename)
                .writeText(xml, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }.onSuccess {
            add(filename)
        }.onFailure {
            logger.error("Failed to write configuration file $filename", it)
        }
    }

    private fun String.replaceChecked(from: String, to: String) =
        replace(from, to.replace("\"", "&quot;"))
}