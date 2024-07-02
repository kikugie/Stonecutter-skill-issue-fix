package dev.kikugie.stonecutter.configuration

import dev.kikugie.stonecutter.StonecutterBuild
import dev.kikugie.stonecutter.StonecutterSetup
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.tooling.model.Model
import org.gradle.tooling.provider.model.ToolingModelBuilder

interface StonecutterModel : Model {
    val setup: StonecutterSetup
    val data: StonecutterDataView
}

internal class StonecutterModelImpl(project: Project) : StonecutterModel {
    override val setup: StonecutterSetup
    override val data: StonecutterDataView

    init {
        val extension = project.extensions.findByType<StonecutterBuild>()
        requireNotNull(extension) { "Stonecutter build extension not found" }

        setup = extension.setup
        data = extension.data
    }
}

internal object StonecutterModelBuilder : ToolingModelBuilder {
    override fun canBuild(name: String): Boolean = name == StonecutterModel::class.java.name
    override fun buildAll(name: String, project: Project): Any = StonecutterModelImpl(project)
}