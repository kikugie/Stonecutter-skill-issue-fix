package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.stonecutter.configuration.StonecutterDataView
import dev.kikugie.stonecutter.configuration.readBuildModel
import dev.kikugie.stonecutter.configuration.readControllerModel
import dev.kikugie.stonecutter.intellij.util.memoize
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import java.nio.file.Path
import kotlin.io.path.Path

private const val CACHE_PATH = "build/stonecutter-cache/model.yml"

fun PsiElement.getStonecutterService() =
    project.service<StonecutterService>()

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.service<StonecutterService>().reset()
    }
}

@Service(PROJECT)
class StonecutterService {
    internal val modulePaths = memoize<Module, _> {
        val path = ExternalSystemApiUtil.getExternalProjectPath(it)
            ?: return@memoize Result.failure(IllegalArgumentException("Module ${it.name} doesn't have a path"))
        Result.success(Path(path))
    }
    internal val controllerModels = memoize<Path, _> {
        readControllerModel(it.resolve(CACHE_PATH))
    }
    internal val buildModels = memoize<Path, _> {
        readBuildModel(it.resolve(CACHE_PATH))
    }

    internal fun getModuleModel(module: Module) = modulePaths(module).getOrNull()?.let(buildModels)
    internal fun getProjectModels(module: Module): Map<StonecutterProject, StonecutterDataView>? {
        val parentPath = modulePaths(module).getOrNull()?.parent?.parent ?: return null
        val controllerModel = controllerModels(parentPath).getOrNull() ?: return null
        return controllerModel.versions.mapNotNull {
            val subDir = parentPath.resolve("versions/${it.project}")
            val buildModel = buildModels(subDir).getOrNull() ?: return@mapNotNull null
            it to buildModel
        }.takeIf { it.isNotEmpty() }?.toMap()
    }

    internal fun reset() {
        modulePaths.clear()
        controllerModels.clear()
        buildModels.clear()
    }
}