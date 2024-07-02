package dev.kikugie.fletching_table.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import dev.kikugie.stonecutter.StonecutterController
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.GradleProject
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.io.File

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.getServiceIfCreated(StonecutterService::class.java)?.reload()
    }
}

@Service(Service.Level.PROJECT)
class StonecutterService(private val project: Project) {
    private val controllers: MutableMap<Module, StonecutterController> = mutableMapOf()
    private val connector = GradleConnector.newConnector().apply {
        forProjectDirectory(project.basePath?.let(::File) ?: return@apply)
    }

    init {
        reload()
    }

    internal fun reload() {
        val cache = ExternalProjectDataCache.getInstance(project)
        val manager = ModuleManager.getInstance(project)

        val connection = connector.connect()
        val model = connection.model(GradleProject::class.java)
    }
}