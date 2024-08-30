package dev.kikugie.stonecutter.controller

/**
 * Parameters applied globally in [StonecutterController].
 */
interface ControllerParameters {
    /**
     * Sets up platform constants (like `fabric`, `forge`, etc.)
     * from project properties.
     *
     * This replaces the need for
     * ```kt
     * // build.gradle.kts
     * val platform = property("loom.platform").toString()
     * stonecutter {
     *     const("fabric", platform == "fabric")
     *     const("forge", platform == "forge")
     *     ...
     * }
     * ```
     */
    val automaticPlatformConstants: Boolean
}