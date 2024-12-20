package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.StonecutterAPI
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.manager.ControllerManager
import dev.kikugie.stonecutter.controller.manager.controller
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.container.TreeBuilderContainer
import dev.kikugie.stonecutter.data.container.getContainer
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import dev.kikugie.stonecutter.data.tree.LightBranch
import dev.kikugie.stonecutter.data.tree.LightNode
import dev.kikugie.stonecutter.data.tree.LightTree
import dev.kikugie.stonecutter.data.tree.TreeBuilder
import dev.kikugie.stonecutter.projectPath
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal typealias BranchEntry = Pair<LightBranch, StonecutterProject>

abstract class ControllerAbstraction(protected val root: Project) {
    protected val parameters: GlobalParameters = GlobalParameters()
    protected val configurations: MutableMap<BranchEntry, ParameterHolder> = mutableMapOf()
    protected val builds: MutableList<Action<StonecutterBuild>> = mutableListOf()
    private val manager: ControllerManager = checkNotNull(root.controller()) {
        "Project ${root.path} is not a Stonecutter controller. What did you even do to get this error?"
    }

    @StonecutterAPI val tree: LightTree = constructTree()
    @StonecutterAPI val vcsVersion: StonecutterProject get() = tree.vcs
    @StonecutterAPI val versions: Collection<StonecutterProject> get() = tree.versions
    @StonecutterAPI val current: StonecutterProject get() = tree.current
    @StonecutterAPI val chiseled: Class<ChiseledTask> = ChiseledTask::class.java

    // link: wiki-controller-active
    /**
     * Sets the active project. **DO NOT call on your own**.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#active-version">Wiki page</a>
     */
    @StonecutterAPI infix fun active(name: Identifier) = with(tree) {
        current.isActive = false
        current = versions.find { it.project == name } ?: error("Project $name is not registered in ${root.path}")
        current.isActive = true
    }

    // link: wiki-chisel
    /**
     * Registers the task as chiseled. This is required for all tasks that need to build all versions.
     *
     * @see [ChiseledTask]
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#chiseled-tasks">Wiki page</a>
     */
    @StonecutterAPI infix fun registerChiseled(provider: TaskProvider<*>) {
        parameters.addTask(provider.name)
    }

    @StonecutterAPI infix fun parameters(configuration: Action<ParameterHolder>) = tree.branches.asSequence()
        .flatMap { b -> versions.map { b to it } }
        .forEach {
            configurations.getOrPut(it) { ParameterHolder(it.first, it.second) }.let(configuration::execute)
        }

    @StonecutterAPI infix fun configureEach(configuration: Action<StonecutterBuild>) {
        builds += configuration
    }

    protected fun updateController(version: StonecutterProject) =
        manager.updateHeader(root.buildFile.toPath(), version.project)

    private fun constructTree(): LightTree {
        val builder: TreeBuilder = checkNotNull(root.gradle.getContainer<TreeBuilderContainer>()[root]) {
            "Project ${root.path} is not registered. This might've been caused by removing a project while its active"
        }
        val branches = builder.nodes.mapValues { (name, nodes) ->
            val branch = if (name.isEmpty()) root else root.project(name)
            val versions = nodes.associate {
                it.project to LightNode(branch.project(it.project).projectPath, it)
            }
            LightBranch(branch.projectPath, name, versions).also {
                versions.forEach { (_, v) -> v.branch = it }
            }
        }
        return LightTree(root.projectPath, ProjectHierarchy(root.path), builder.vcsProject, branches).also {
            branches.forEach { (_, b) -> b.tree = it }
        }
    }
}