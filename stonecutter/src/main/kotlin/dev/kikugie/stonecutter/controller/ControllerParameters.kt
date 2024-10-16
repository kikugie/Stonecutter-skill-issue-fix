package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.Identifier

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

    /**
     * Specifies the default version checked in predicates.
     * Defaults to `minecraft`, making the following checks equivalent:
     * ```
     * //? if <1.21
     * //? if minecraft: <1.21
     * ```
     * Modifying this property changes the receiver name.
     */
    val defaultReceiver: Identifier

    /**
     * Debug mode toggle for all nodes in the tree.
     *
     * Currently, debug includes the following functionality:
     * - File processing caches are disabled in `build/stonecutter-cache/`:
     *      - `transform_parameters.yml` is generated every time.
     *      - `checksums.cbor` is ignored.
     *      - `results/` is ignored.
     * - Debug ASTs in `.yml` format are written to `build/stonecutter-cache/debug`.
     * - All processing steps are written in the main log.
     */
    val debug: Boolean
}