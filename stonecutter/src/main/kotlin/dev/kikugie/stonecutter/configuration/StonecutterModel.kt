package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterController
import dev.kikugie.stonecutter.StonecutterProject
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.tooling.model.Model
import org.gradle.tooling.provider.model.ToolingModelBuilder
import java.io.Serializable

interface StonecutterBuildModel : Model {
    val current: StonecutterProject
    val data: StonecutterDataView
}

data class StonecutterBuildModelImpl(
    override val current: StonecutterProject,
    override val data: StonecutterDataView
) : StonecutterBuildModel, Serializable {
   internal constructor(plugin: StonecutterBuild) : this(plugin.current, plugin.data)
}

class StonecutterBuildModelBuilder : ToolingModelBuilder {
    override fun canBuild(name: String): Boolean =
        name == StonecutterBuildModel::class.qualifiedName!!

    override fun buildAll(name: String, project: Project): StonecutterBuildModel =
        StonecutterBuildModelImpl(project.extensions.getByType<StonecutterBuild>())
}

interface StonecutterControllerModel : Model {
    val versions: List<StonecutterProject>
    val vcsVersion: StonecutterProject
    var current: StonecutterProject
}

data class StonecutterControllerModelImpl(
    override val versions: List<StonecutterProject>,
    override val vcsVersion: StonecutterProject,
    override var current: StonecutterProject
) : StonecutterControllerModel, Serializable {
    internal constructor(controller: StonecutterController) : this(controller.setup.versions, controller.setup.vcsVersion, controller.setup.current)
}

class StonecutterControllerModelBuilder : ToolingModelBuilder {
    override fun canBuild(name: String): Boolean =
        name == StonecutterControllerModel::class.qualifiedName!!

    override fun buildAll(name: String, project: Project): StonecutterControllerModel =
        StonecutterControllerModelImpl(project.extensions.getByType<StonecutterController>())
}