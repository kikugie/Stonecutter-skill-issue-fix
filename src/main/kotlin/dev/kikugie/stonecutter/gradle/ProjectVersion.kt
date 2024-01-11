package dev.kikugie.stonecutter.gradle

data class ProjectVersion(
    private val plugin: StonecutterBuild,
    private val subProject: SubProject
) {
    /**
     * Minecraft version of the subproject.
     */
    val version = subProject.version

    /**
     * Subproject's entry in `./versions/`.
     */
    val project = subProject.project

    /**
     * Whenever the subproject is currently processed by Stonecutter.
     */
    val isActive: Boolean
        get() = project == plugin.setup.current.project
}