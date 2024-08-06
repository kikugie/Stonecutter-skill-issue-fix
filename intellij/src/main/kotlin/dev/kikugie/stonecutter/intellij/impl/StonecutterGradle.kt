package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.configuration.StonecutterControllerModel
import dev.kikugie.stonecutter.intellij.util.memoize
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.Model
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KClass

val REGEX = Regex("stonecutter\\.gradle(\\.kts)?")

fun PsiElement.getStonecutterService() = ModuleUtil
    .findModuleForPsiElement(this)?.let {module ->
        // Temp unpack for debugging
        val project = project
        val service = project.service<StonecutterService>()
        val model = service.controllerModels(module)
        model
    }

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.service<StonecutterService>().reset()
    }
}

@Service(PROJECT)
class StonecutterService(root: Project) {
    internal val controllerModels = memoize<Module, _> {
        val dir = Path(ExternalSystemApiUtil.getExternalProjectPath(it) ?: return@memoize null)
        val parentPath = dir.parent.parent // Drop /versions/<this>
        getModel<StonecutterControllerModel>(parentPath.toFile())
    }

    internal fun reset() {
        controllerModels.clear()
    }

    private inline fun <reified T : Model> getModel(dir: File): T? = getModel(dir, T::class)

    private fun <T : Model> getModel(dir: File, type: KClass<T>): T? = runCatching {
        val connector = GradleConnector.newConnector().apply {
            forProjectDirectory(dir)
        }
        connector.connect().use {
            it.getModel(type.java)
        }
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()
}