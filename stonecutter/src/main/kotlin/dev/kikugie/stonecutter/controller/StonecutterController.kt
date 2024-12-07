package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.build.BuildParameters
import dev.kikugie.stonecutter.build.StonecutterBuild
import dev.kikugie.stonecutter.controller.manager.ControllerManager
import dev.kikugie.stonecutter.controller.manager.controller
import dev.kikugie.stonecutter.controller.storage.GlobalParameters
import dev.kikugie.stonecutter.controller.storage.ProjectBranch
import dev.kikugie.stonecutter.controller.storage.ProjectNode
import dev.kikugie.stonecutter.controller.storage.ProjectTree
import dev.kikugie.stonecutter.data.container.ProjectParameterContainer
import dev.kikugie.stonecutter.data.container.ProjectTreeContainer
import dev.kikugie.stonecutter.data.container.TreeBuilderContainer
import dev.kikugie.stonecutter.data.model.BranchInfo.Companion.toBranchInfo
import dev.kikugie.stonecutter.data.model.NodeInfo.Companion.toNodeInfo
import dev.kikugie.stonecutter.data.model.BranchModel
import dev.kikugie.stonecutter.data.model.TreeModel
import dev.kikugie.stonecutter.process.StonecutterTask
import dev.kikugie.stonecutter.settings.builder.TreeBuilder
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

internal typealias BranchEntry = Pair<ProjectBranch, StonecutterProject>

// link: wiki-controller
/**
 * Stonecutter plugin applied to `stonecutter.gradle[.kts]`.
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#controller-stonecutter-gradle-kts">Wiki page</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StonecutterController(internal val root: Project) : StonecutterUtility, ControllerParameters {
    private val manager: ControllerManager = checkNotNull(root.controller()) {
        "Project ${root.path} is not a Stonecutter controller. What did you even do to get this error?"
    }
    private val treeContainer: ProjectTreeContainer = root.gradle.extensions.getByType<ProjectTreeContainer>()
    private val parameterContainer: ProjectParameterContainer =
        root.gradle.extensions.getByType<ProjectParameterContainer>()
    private val configurations: MutableMap<BranchEntry, ParameterHolder> = mutableMapOf()
    private val builds: MutableList<Action<StonecutterBuild>> = mutableListOf()
    private val parameters: GlobalParameters = GlobalParameters()

    /**The full project tree this controller operates on. The default branch is `""`.*/
    val tree: ProjectTree

    /**Version control project used by `Reset active project` task.*/
    val vcsVersion: StonecutterProject get() = tree.vcs

    /**All versions registered in the tree. Branches may have the same or a subset of these.*/
    val versions: Collection<StonecutterProject> get() = tree.versions

    // link: wiki-controller-active
    /**
     * The active version selected by `stonecutter.active "..."` call.
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#active-version">Wiki page</a>
     */
    val current: StonecutterProject get() = tree.current

    /**Type of the chiseled task. Used with [registerChiseled].*/
    val chiseled: Class<ChiseledTask> = ChiseledTask::class.java

    override var automaticPlatformConstants: Boolean = false
    override var debug: Boolean by parameters.named("debug")
    override var processFiles: Boolean by parameters.named("process")
    override var defaultReceiver: Identifier by parameters.named("receiver") {
        require(it.isValid()) { "Invalid receiver '$it'" }
    }

    init {
        val data: TreeBuilder = checkNotNull(root.gradle.extensions.getByType<TreeBuilderContainer>()[root]) {
            "Project ${root.path} is not registered. This might've been caused by removing a project while its active"
        }
        tree = constructTree(data).also(::configureTree)
        root.afterEvaluate { setupProject() }
    }

    // link: wiki-controller-active
    /**
     * Sets the active project. **DO NOT call on your own**
     *
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#active-version">Wiki page</a>
     */
    infix fun active(name: Identifier) = with(tree) {
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
    infix fun registerChiseled(provider: TaskProvider<*>) {
        parameters.addTask(provider.name)
    }

    // link: wiki-controller-params
    /**
     * Specifies configurations for all combinations of versions and branches.
     * This provides parameters for the processor to use non-existing versions.
     * If the given version exists, it will be applied to the [StonecutterBuild].
     *
     * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#global-parameters">Wiki page</a>
     */
    infix fun parameters(configuration: Action<ParameterHolder>) = tree.branches.asSequence().flatMap { br ->
        versions.map { br to it }
    }.forEach {
        configurations.getOrPut(it) { ParameterHolder(it.first, it.second) }.let(configuration::execute)
    }

    /**
     * Executes the provided action on each node after the nodes are configured.
     *
     * This may miss configurations required for a multi-branch setup
     * and may have issues accessing versioned project properties.
     *
     * @param configuration Versioned plugin configuration
     */
    @Deprecated(message = "Use `parameters {}` for global configuration.")
    infix fun configureEach(configuration: Action<StonecutterBuild>) {
        builds += configuration
    }

    private fun constructTree(model: TreeBuilder): ProjectTree = model.nodes.mapValues { (name, nodes) ->
        val branch = if (name.isEmpty()) root else root.project(name)
        val versions = nodes.associate {
            it.project to ProjectNode(branch.project(it.project), it)
        }
        ProjectBranch(branch, name, versions)
    }.let {
        ProjectTree(root, model.vcsProject, it)
    }

    private fun configureTree(tree: ProjectTree) {
        (tree.branches + tree.nodes).forEach {
            treeContainer.register(it, tree)
            parameterContainer.register(it, parameters)
        }
        val task = root.tasks.create("chiseledStonecutter")
        for (it in tree.nodes) {
            it.project.pluginManager.apply(StonecutterPlugin::class)
            task.dependsOn("${it.project.path}:setupChiseledBuild")
        }
    }

    private fun setupProject() {
        if (automaticPlatformConstants) configurePlatforms(tree.nodes)
        tree.nodes.forEach {
            configurations[it.branch to it.metadata]?.run { it.stonecutter.from(this) }
            for (build in builds) build.execute(it.stonecutter)
        }

        createStonecutterTask("Reset active project", tree.vcs) {
            "Sets active version to ${tree.vcs.project}. Run this before making a commit."
        }
        createStonecutterTask("Refresh active project", tree.current) {
            "Runs the comment processor on the active version. Useful for fixing comments in wrong states."
        }
        for (it in versions) createStonecutterTask("Set active project to ${it.project}", it) {
            "Sets the active project to ${it.project}, processing all versioned comments."
        }

        serializeTree()
        serializeBranches()
    }

    private inline fun createStonecutterTask(name: String, version: StonecutterProject, desc: () -> String) =
        createStonecutterTask(name, version, desc())

    private fun createStonecutterTask(
        name: String,
        version: StonecutterProject,
        desc: String,
    ) {
        root.tasks.create<StonecutterTask>(name) {
            val builds = tree.branches.associateWith {
                it[version.project]?.stonecutter?.data
                    ?: configurations[it to version]?.data
                    ?: BuildParameters()
            }
            val paths = tree.branches.associateWith { it.path }

            group = "stonecutter"
            description = desc

            params.set(parameters)
            toVersion.set(version)
            fromVersion.set(tree.current)

            input.set("src")
            output.set("src")

            data.set(builds)
            sources.set(paths)
            cacheDir.set { branch, version -> branch.cachePath(version) }

            doLast {
                manager.updateHeader(this.project.buildFile.toPath(), version.project)
            }
        }
    }

    private fun configurePlatforms(projects: Iterable<Project>) {
        val key = "loom.platform"
        val platforms = buildSet {
            for (it in projects) it.findProperty(key)?.run {
                add(toString())
            }
        }.also { if (it.isEmpty()) return }
        parameters {
            val platform = node?.findProperty(key)?.toString() ?: "\n"
            consts(platform, platforms)
        }
    }

    private fun serializeTree() = with(tree) {
        TreeModel(
            STONECUTTER,
            vcsVersion,
            current,
            branches.map { it.toBranchInfo(path.relativize(it.path)) },
            nodes.map { it.toNodeInfo(path.relativize(it.location), current) },
            parameters,
        ).save(stonecutterCachePath).onFailure {
            logger.warn("Failed to save tree model", it)
        }
    }

    private fun serializeBranches() = tree.branches.onEach {
        BranchModel(
            id,
            path.relativize(tree.path),
            nodes.map { it.toNodeInfo(it.location.relativize(tree.path), current) },
        ).save(stonecutterCachePath).onFailure {
            logger.warn("Failed to save branch model for '$name'", it)
        }
    }
}