package dev.kikugie.stonecutter.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType


open class StonecutterController(project: Project) {
    private val controller: ControllerManager = project.controller()
        ?: throw StonecutterGradleException("Project ${project.path} is not a Stonecutter controller. What did you even do to get this error?")
    private val setup: StonecutterConfiguration =
        project.gradle.extensions.getByType(StonecutterConfiguration.Container::class.java)[project]
            ?: throw StonecutterGradleException("Project ${project.path} is not registered. This might've been caused by removing a project while its active")

    val versions: List<StonecutterProject> get() = setup.versions
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java

    init {
        versions.forEach { project.project(it.project).pluginManager.apply(StonecutterPlugin::class.java) }
        project.tasks.create("chiseledStonecutter") {
            setup.versions.forEach { dependsOn("${it.project}:setupChiseledBuild") }
        }
        project.afterEvaluate { setupProject(this) }
    }

    infix fun active(str: String) {
        setup.current = setup.versions.find { it.project == str }?.asActive()
            ?: throw GradleException("[Stonecutter] Project $str is not registered")
    }

    infix fun registerChiseled(provider: TaskProvider<*>) {
        setup.register(provider.name)
    }

    private fun setupProject(root: Project) {
        val vcsProject = root.project(setup.vcsVersion.project)
        val vcs = setup.vcsVersion
        root.tasks.create(
            "Reset active project", StonecutterTask::class.java
        ).applyConfig(root, vcsProject, vcs)
        val active = setup.current
        root.tasks.create(
            "Refresh active project", StonecutterTask::class.java
        ).applyConfig(root, root.project(active.project), active)

        setup.versions.forEach { ver ->
            val project = root.project(ver.project)
            root.tasks.create(
                "Set active project to ${ver.project}", StonecutterTask::class.java
            ).applyConfig(root, project, ver)
        }
    }

    private fun StonecutterTask.applyConfig(root: Project, subproject: Project, version: StonecutterProject) {
        group = "stonecutter"

        toVersion.set(version)
        val build = subproject.extensions.getByType<StonecutterBuild>()
        constants.set(build.constants)
        expressions.set(build.expressions)
        swaps.set(build.swaps)
        filter.set { p -> if (build.filters.isEmpty()) true else build.filters.all { it(p) } }

        input.set(root.file("./src").toPath())
        output.set(input.get())

        doLast {
            controller.updateHeader(this.project.buildFile.toPath(), version.project)
        }
    }
}