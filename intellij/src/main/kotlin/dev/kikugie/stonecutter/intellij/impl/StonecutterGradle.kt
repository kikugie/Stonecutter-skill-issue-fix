package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.configuration.StonecutterDataView
import dev.kikugie.stonecutter.configuration.readBuildModel
import dev.kikugie.stonecutter.configuration.readControllerModel
import dev.kikugie.stonecutter.intellij.util.memoize
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import java.nio.file.Path
import java.util.Optional
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

private const val CACHE_PATH = "build/stonecutter-cache/model.yml"

val PsiElement.stonecutterService get() =
    project.getService(StonecutterService::class.java)

val PsiElement.module get() = ModuleUtil.findModuleForPsiElement(this)

val Path.doubleParent get() = parent.parent

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.service<StonecutterService>().reset()
    }
}

@Service(PROJECT)
class StonecutterService {
    private val modulePaths = memoize<Module, _> {
        Optional.ofNullable(ExternalSystemApiUtil.getExternalProjectPath(it)?.let(::Path))
    }
    private val controllerModels = memoize<Path, _> {
        readControllerModel(it.resolve(CACHE_PATH))
    }
    private val buildModels = memoize<Path, _> {
        readBuildModel(it.resolve(CACHE_PATH))
    }
    private val collectedBuildModels = memoize<Module, _> {
        val parentPath = modulePaths(it).getOrNull()?.doubleParent ?: return@memoize emptyMap()
        val controllerModel = controllerModels(parentPath).getOrNull() ?: return@memoize emptyMap()
        controllerModel.versions.mapNotNull { proj ->
            val subDir = parentPath.resolve("versions/${proj.project}")
            val buildModel = buildModels(subDir).getOrNull() ?: return@mapNotNull null
            proj to buildModel
        }.toMap()
    }

    fun getModulePath(module: Module) = modulePaths(module)
    fun getModuleModel(module: Module) = modulePaths(module).getOrNull()?.let(buildModels)
    fun getProjectModels(module: Module): Map<StonecutterProject, StonecutterDataView> = collectedBuildModels(module)

    internal fun reset() {
        modulePaths.clear()
        controllerModels.clear()
        buildModels.clear()
        collectedBuildModels.clear()
    }
}