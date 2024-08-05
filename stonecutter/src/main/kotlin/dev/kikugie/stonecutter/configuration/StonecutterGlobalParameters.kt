package dev.kikugie.stonecutter.configuration

interface StonecutterGlobalParameters {
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