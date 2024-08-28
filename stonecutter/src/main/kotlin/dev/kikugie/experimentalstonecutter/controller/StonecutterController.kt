package dev.kikugie.experimentalstonecutter.controller

import dev.kikugie.experimentalstonecutter.ProjectName
import dev.kikugie.experimentalstonecutter.StonecutterProject
import dev.kikugie.stonecutter.configuration.stonecutterCachePath
import dev.kikugie.stonecutter.*
import dev.kikugie.experimentalstonecutter.StonecutterUtility
import dev.kikugie.experimentalstonecutter.build.StonecutterBuild
import dev.kikugie.experimentalstonecutter.data.TreeContainer
import dev.kikugie.experimentalstonecutter.data.TreeModel
import dev.kikugie.experimentalstonecutter.data.TreeModelContainer
import dev.kikugie.stonecutter.process.StonecutterTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterController(internal val root: Project) : StonecutterUtility, ControllerParameters {
    private val manager: ControllerManager = checkNotNull(root.controller()) {
        "Project ${root.path} is not a Stonecutter controller. What did you even do to get this error?"
    }
    private val container = root.gradle.extensions.getByType<TreeContainer>()
    private val tree: ProjectTree

    val vcsVersion get() = tree.vcs
    val versions: List<StonecutterProject> get() = tree.versions
    val current: StonecutterProject get() = tree.current
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java

    override var automaticPlatformConstants: Boolean = false

    init {
        println("Running Stonecutter 0.5-alpha.4")
        val data: TreeModel = checkNotNull(root.gradle.extensions.getByType<TreeModelContainer>()[root]) {
            "Project ${root.path} is not registered. This might've been caused by removing a project while its active"
        }
        val branches = mutableMapOf<ProjectName, ProjectBranch>()
        for ((name, branch) in data.nodes) {
            val branchProject = if (name.isEmpty()) root else root.project(name)
            val versions = mutableMapOf<ProjectName, ProjectNode>()
            for (ver in branch) {
                val nodeProject = branchProject.project(ver.project)
                versions[ver.project] = ProjectNode(nodeProject, name, ver)
            }
            branches[name] = ProjectBranch(branchProject, name, versions)
        }
        tree = ProjectTree(root, data.vcsVersion, branches)
        tree.versions = data.versions.toList()
        container.register(root.path, tree)
        tree.branches.values.forEach {
            container.register(it.project.path, tree)
        }

        root.afterEvaluate { setupProject() }
    }

    infix fun active(name: ProjectName) = with(tree) {
        current.isActive = false
        current = versions.find { it.project == name } ?: error("Project $name is not registered in ${root.path}")
        current.isActive = true
    }

    infix fun registerChiseled(provider: TaskProvider<*>) {
        tree.addTask(provider.name)
    }

    operator fun get(project: ProjectName) = tree[project]

    private fun configurePlatforms(projects: Iterable<Project>) {
        val key = "loom.platform"
        val platforms = mutableSetOf<String>()
        for (it in projects) it.findProperty(key)?.run {
            platforms += this.toString()
        }
        if (platforms.isEmpty()) return
        for (it in projects) it.findProperty(key)?.run {
            it.extensions.getByType<StonecutterBuild>().consts(this.toString(), platforms)
        }
    }

    private fun setupProject() {
        createStonecutterTask("Reset active project", tree.vcs) {
            "Sets active version to ${tree.vcs.project}. Run this before making a commit."
        }
        createStonecutterTask("Refresh active project", tree.current) {
            "Runs the comment processor on the active version. Useful for fixing comments in wrong states."
        }
        for (it in versions) createStonecutterTask("Set active project to ${it.project}", it) {
            "Sets the active project to ${it.project}, processing all versioned comments."
        }
        if (automaticPlatformConstants) configurePlatforms(
            tree.branches.values.flatMap { it.entries.values.map(ProjectNode::project) }
        )
    }

    private inline fun createStonecutterTask(name: String, version: StonecutterProject, desc: () -> String) =
        createStonecutterTask(name, version, desc())

    private fun createStonecutterTask(
        name: String,
        version: StonecutterProject,
        desc: String,
    ) {
        root.tasks.create<StonecutterTask>(name) {
            group = "stonecutter"
            description = desc

            toVersion.set(version)
            fromVersion.set(tree.current)

            input.set("src")
            output.set("src")

            dests.set(tree.branches.mapValues { (_, v) -> v.project.projectDir.toPath() })
            cacheDir.set { branch, version -> tree[branch][version.project]?.project?.stonecutterCachePath
                ?: tree[branch]!!.project.stonecutterCachePath.resolve("out-of-bounds/$version")
            }

            doLast {
                manager.updateHeader(this.project.buildFile.toPath(), version.project)
            }
        }
    }
}