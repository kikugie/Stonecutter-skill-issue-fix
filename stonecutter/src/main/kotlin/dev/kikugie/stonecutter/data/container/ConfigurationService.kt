package dev.kikugie.stonecutter.data.container

import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.parameters.BuildParameters
import dev.kikugie.stonecutter.data.parameters.GlobalParameters
import dev.kikugie.stonecutter.data.tree.LightTree
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import javax.inject.Inject

internal typealias HierarchyMap<T> = MapProperty<ProjectHierarchy, T>

internal abstract class ConfigurationService @Inject constructor() : BuildService<ConfigurationService.Parameters> {
    fun of(path: ProjectHierarchy) = HierarchyScope(path)
    fun snapshot(): Snapshot = Snapshot(
        trees = parameters.projectTrees.get(),
        global = parameters.globalParameters.get(),
        build = parameters.buildParameters.get()
    )

    interface Parameters : BuildServiceParameters {
        val projectTrees: HierarchyMap<LightTree>
        val globalParameters: HierarchyMap<GlobalParameters>
        val buildParameters: HierarchyMap<BuildParameters>
    }

    @Deprecated("TODO: Build service integration doesn't work well so far, but making the data serializable does.")
    data class Snapshot(
        val trees: Map<ProjectHierarchy, LightTree>,
        val global: Map<ProjectHierarchy, GlobalParameters>,
        val build: Map<ProjectHierarchy, BuildParameters>
    )

    inner class HierarchyScope(private val path: ProjectHierarchy) {
        val tree: LightTree? get() = parameters.projectTrees[path]
        val global: GlobalParameters? get() = parameters.globalParameters[path]
        val build: BuildParameters? get() = parameters.buildParameters[path]
    }

    companion object {
        const val NAME = "stonecutter-config"
        internal fun Provider<ConfigurationService>.of(path: ProjectHierarchy) = get().of(path)
        internal operator fun <T> HierarchyMap<T>.get(path: ProjectHierarchy): T? = get()[path]
    }
}