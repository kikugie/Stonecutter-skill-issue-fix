package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.*
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.hierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import dev.kikugie.stonecutter.data.tree.*
import dev.kikugie.stonecutter.ide.IdeaSetupTask
import dev.kikugie.stonecutter.process.StonecutterTask
import org.gradle.api.Project
import org.gradle.internal.DefaultTaskExecutionRequest
import org.gradle.kotlin.dsl.register

/**
 * Stonecutter plugin applied to `stonecutter.gradle[.kts]`.
 *
 * @see <a href="https://stonecutter.kikugie.dev/stonecutter/guide/setup#controller-stonecutter-gradle-kts">Wiki page</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
public open class StonecutterController(root: Project) :
    ControllerAbstraction(root),
    StonecutterUtility,
    GlobalParametersAccess {
    override var generateRunConfigs: Collection<RunConfigType> = setOf(RunConfigType.SWITCH, RunConfigType.CHISEL)
    override var automaticPlatformConstants: Boolean = false
    override var debug: Boolean by parameters.named("debug")
    override var processFiles: Boolean by parameters.named("process")
    override var defaultReceiver: Identifier by parameters.named("receiver") {
        require(it.isValid()) { "Invalid receiver '$it'" }
    }

    init {
        prepareConfiguration()
        root.afterEvaluate { configureProject() }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(StonecutterDelicate::class)
    private fun prepareConfiguration() = with(StonecutterPlugin.SERVICE()) {
        val maps: Array<MutableMap<ProjectHierarchy, Any>> = arrayOf(mutableMapOf(), mutableMapOf(), mutableMapOf())
        for (it in buildSet {
            add(tree.hierarchy)
            addAll(tree.branches.map(LightBranch::hierarchy))
            tree.versions.flatMap { v -> tree.branches.map { it.hierarchy + v.project } }
                .let(::addAll)
        }) {
            maps[0][it] = tree
            maps[1][it] = BuildParameters()
            maps[2][it] = this@StonecutterController.parameters
        }
        parameters.projectTrees.set(maps[0] as Map<ProjectHierarchy, LightTree>)
        parameters.buildParameters.set(maps[1] as Map<ProjectHierarchy, BuildParameters>)
        parameters.globalParameters.set(maps[2] as Map<ProjectHierarchy, GlobalParameters>)

        val syncTask = root.tasks.create("chiseledStonecutter")
        for (it in tree.nodes) {
            withProject(it).pluginManager.apply(StonecutterPlugin::class.java)
            syncTask.dependsOn("${it.hierarchy}:setupChiseledBuild")
        }
    }

    @OptIn(StonecutterDelicate::class)
    private fun configureProject() {
        for (it in tree.nodes) {
            val plugin = withProject(it).stonecutter
            configurations[it.branch to it.metadata]?.run { plugin.from(this) }
            builds.onEach { execute(plugin) }
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
        setupConfigurationTasks()
    }

    private fun setupConfigurationTasks() = with(root.rootProject) {
        if ("stonecutterIdea" !in tasks.names) {
            val parameters = StonecutterPlugin.SERVICE().parameters
            val uniqueTrees = parameters.projectTrees()
                .values.distinctBy { it.hierarchy }
            val rootProjects = uniqueTrees
                .map { it.hierarchy }.toSet()
            val chiseledTasks = parameters.globalParameters()
                .filter { (key, _) -> key in rootProjects }
                .mapValues { (_, value) -> value.chiseled }
            tasks.register<IdeaSetupTask>("stonecutterIdea") {
                group = "ide"
                types.set(generateRunConfigs)
                trees.set(uniqueTrees)
                tasks.set(chiseledTasks)
            }
            if (System.getProperty("idea.sync.active", "false").toBoolean()) gradle.startParameter.let { st ->
                if (st.taskRequests.none { "stonecutterIdea" in it.args })
                    st.setTaskRequests(st.taskRequests + DefaultTaskExecutionRequest(listOf("stonecutterIdea")))
            }
        }
    }

    private fun createStonecutterTask(name: String, version: StonecutterProject, desc: () -> String) =
        root.tasks.register<StonecutterTask>(name) {
            group = "Stonecutter"
            description = desc()

            instance(project.hierarchy)

            fromVersion(current)
            toVersion(version)

            input("src")
            output("src")
            sources.set(tree.branches)

            parameters(StonecutterPlugin.SERVICE().snapshot())
            doLast { updateController(version) }
        }

    private fun serializeTree() = with(tree) {
        TreeModel(
            STONECUTTER,
            vcsVersion,
            current,
            branches.map {
                BranchInfo(it.id, location cut it.location)
            },
            nodes.map {
                NodeInfo(it.metadata, location cut it.location, it.metadata.isActive)
            },
            parameters
        ).save(tree.location.resolve("build/stonecutter-cache")).onFailure {
            root.logger.warn("Failed to save tree model", it)
        }
    }

    private fun serializeBranches() = tree.branches.onEach {
        BranchModel(
            id,
            location.relativize(tree.location),
            nodes.map {
                NodeInfo(it.metadata, location cut it.location, it.metadata.isActive)
            }
        ).save(tree.location.resolve("build/stonecutter-cache")).onFailure {
            root.logger.warn("Failed to save branch model for '$id'", it)
        }
    }
}