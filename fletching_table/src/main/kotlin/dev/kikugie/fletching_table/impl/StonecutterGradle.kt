package dev.kikugie.fletching_table.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import dev.kikugie.stonecutter.configuration.StonecutterModel
import dev.kikugie.stonecutter.process.decode
import dev.kikugie.stonecutter.process.runIgnoring
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.io.File
import java.util.WeakHashMap
import kotlin.io.path.isReadable
import kotlin.io.path.notExists

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.getService(StonecutterService::class.java).reload()
    }
}

@Service(Service.Level.PROJECT)
class StonecutterService(private val root: Project) {
    private val models: MutableMap<Module, StonecutterModel> = WeakHashMap()

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
            val project = cache.getRootExternalProject(it.path) ?: return@forEach
            val buildFileName = project.buildFile?.name
            if (buildFileName == "stonecutter.gradle" || buildFileName == "stonecutter.gradle.kts")
                loadVersions(project, modules)
        }
    }

    private fun loadVersions(project: ExternalProject, modules: Map<File, Module>) {
        for (it in project.childProjects.values) {
            val module = modules[it.projectDir] ?: continue
            val file = it.buildDir.resolve("stonecutterCache/model.bin").toPath()
            if (file.notExists() || !file.isReadable()) continue
            runIgnoring {
                models[module] = file.decode()
            }
        }
    }
}