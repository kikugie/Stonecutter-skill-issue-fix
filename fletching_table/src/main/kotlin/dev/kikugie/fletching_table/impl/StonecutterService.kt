package dev.kikugie.fletching_table.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import dev.kikugie.stonecutter.configuration.StonecutterModel
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.io.File
import com.intellij.openapi.module.Module

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.getService(StonecutterService::class.java).reload()
    }
}

@Service(Service.Level.PROJECT)
class StonecutterService(private val root: Project) {
    private val models: MutableMap<Module, StonecutterModel> = mutableMapOf()

    init {
        reload()
    }

    internal fun reload() {
        models.clear()
        val cache = ExternalProjectDataCache.getInstance(root)
        val manager = ModuleManager.getInstance(root)
        val modules = mutableMapOf<File, Module>()

        for (module in manager.modules) module.getComponent(ModuleRootManager::class.java).contentRoots.forEach {
            modules[File(it.path)] = module
        }

        for (module in manager.modules) module.getComponent(ModuleRootManager::class.java).contentRoots.forEach {
            val child = cache.getRootExternalProject(it.path) ?: return@forEach
            loadProject(child, modules)
        }
    }

    private fun loadProject(project: ExternalProject, modules: Map<File, Module>) {
        try {
            val buildFile = project.buildFile
            if (buildFile?.name != "stonecutter.gradle" && buildFile?.name != "stonecutter.gradle.kts")
                throw Exception()
            loadVersions(project, modules)
        } catch (_: Exception) {
        }
    }

    private fun loadVersions(project: ExternalProject, modules: Map<File, Module>) =
        project.childProjects.values.forEach {
            val file = it.buildDir.resolve("stonecutterCache/model.bin")
        }
}