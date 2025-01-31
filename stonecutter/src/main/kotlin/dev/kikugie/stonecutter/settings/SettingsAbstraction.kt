package dev.kikugie.stonecutter.settings

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.tree.toTree
import dev.kikugie.stonecutter.data.tree.TreeBuilder
import dev.kikugie.stonecutter.data.tree.TreeSettings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File

/**Method variations for [StonecutterSettings]*/
@OptIn(ExperimentalSerializationApi::class)
public abstract class SettingsAbstraction(private val settings: Settings) {
    private lateinit var shared: Action<TreeBuilder>

    /* Shared configuration */
    /**Stores the provided configuration to be used in [create] methods.*/
    @StonecutterAPI public fun shared(action: Action<TreeBuilder>) {
        shared = action
    }

    /* File configuration */
    /**Configures the specified [project] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(project: ProjectPath, file: File): String =
        create(project.project(), file).let { BNAN }

    /**Configures the specified [project] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(project: ProjectDescriptor, file: File): Unit =
        create(listOf(project), file)

    /**Configures the specified [projects] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(vararg projects: ProjectPath, file: File): String =
        create(projects.map { it.project() }, file).let { BNAN }

    /**Configures the specified [projects] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(vararg projects: ProjectDescriptor, file: File): Unit =
        create(projects.toList(), file)

    /**Configures the specified [projects] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectPath>, file: File): String =
        create(projects.map { it.project() }, file).let { BNAN }

    /**Configures the specified [projects] to be versioned with setup provided by [file].*/
    @StonecutterAPI public fun create(projects: Iterable<ProjectDescriptor>, file: File) {
        require(file.extension == "json") { "Version setup file must be in JSON format. See Stonecutter wiki for more information." }
        val data: TreeSettings = file.inputStream().use { LENIENT_JSON.decodeFromStream(it) }
        create(projects, Action(data::toTree))
    }

    /* Action configuration */
    /**Configures the specified [project] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(project: ProjectPath, action: Action<TreeBuilder> = shared): String =
        create(project.project(), action).let { BNAN }

    /**Configures the specified [project] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(project: ProjectDescriptor, action: Action<TreeBuilder> = shared): Unit =
        create(listOf(project), action)
    
    /**Configures the specified [projects] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(vararg projects: ProjectPath, action: Action<TreeBuilder> = shared): String =
        create(projects.map { it.project() }, action).let { BNAN }

    /**Configures the specified [projects] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(vararg projects: ProjectDescriptor, action: Action<TreeBuilder> = shared): Unit =
        create(projects.toList(), action)

    /**Configures the specified [projects] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(projects: Iterable<ProjectPath>, action: Action<TreeBuilder> = shared): String =
        create(projects.map { it.project() }, action).let { BNAN }

    /**Configures the specified [projects] to be versioned with setup provided by [action] or [shared].*/
    @StonecutterAPI @JvmOverloads public fun create(projects: Iterable<ProjectDescriptor>, action: Action<TreeBuilder> = shared): Unit =
        projects.forEach { create(it, TreeBuilder().also(action::execute)) }

    /* Base configuration */
    protected abstract fun create(project: ProjectDescriptor, setup: TreeBuilder)

    protected fun ProjectPath.project(): ProjectDescriptor = removeStarting(':').let {
        if (it.isEmpty()) settings.rootProject
        else {
            settings.include(it)
            settings.project(":$it")
        }
    }
}