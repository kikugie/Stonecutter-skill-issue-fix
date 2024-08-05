package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.configuration.StonecutterControllerModel
import org.gradle.tooling.GradleConnector
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.io.File
import kotlin.reflect.KClass

val REGEX = Regex("stonecutter\\.gradle(\\.kts)?")

fun PsiElement.getStonecutterService() = ModuleUtil
    .findModuleForPsiElement(this)?.let {
        project.getServiceIfCreated(StonecutterService::class.java)?.getControllerModel(it)
    }

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.getServiceIfCreated(StonecutterService::class.java)?.reset()
    }
}

@Service(PROJECT)
class StonecutterService(root: Project) {
    private val externalProjects: ExternalProjectDataCache = ExternalProjectDataCache.getInstance(root)
    private val controllerModels: MutableMap<Module, StonecutterControllerModel?> = mutableMapOf()

    fun getControllerModel(module: Module): StonecutterControllerModel? = controllerModels.computeIfAbsent(module) {
        ModuleRootManager.getInstance(it).contentRoots.firstNotNullOfOrNull {
            val project = externalProjects.getRootExternalProject(it.path) ?: return@firstNotNullOfOrNull null
            if (project.buildFile?.name?.matches(REGEX) != true) return@firstNotNullOfOrNull null
            getModel(project.projectDir, StonecutterControllerModel::class)
        }
    }

    internal fun reset() {
        controllerModels.clear()
    }

    private fun <T : Any> getModel(dir: File, type: KClass<T>): T? = runCatching {
        val connector = GradleConnector.newConnector().apply {
            forProjectDirectory(dir)
        }
        connector.connect().use {
            it.getModel(type.java).also {
                println("Yo")
            }
        }
    }.getOrNull()
}