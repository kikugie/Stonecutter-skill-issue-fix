package dev.kikugie.experimentalstonecutter

import org.gradle.api.Project

/**
 * Represents an existing [StonecutterProject.project].
 */
typealias ProjectName = String

/**
 * Represents a path in Gradle project hierarchy.
 */
typealias ProjectPath = String

/**
 * Represents a version string to be parsed as semver.
 */
typealias TargetVersion = String

/**
 * Represents a name of a Gradle task.
 */
typealias TaskName = String

/**
 * Used as a return value by some configuration methods.
 * Generic type erasure makes `func(iter: Iterable<A>)` and `func(iter: Iterable<B>)`
 * have conflicting signatures on the JVM.
 */
const val BNAN = "🍌"

internal fun ProjectPath.sanitize() = removePrefix(":")

internal val Project.buildDirectory
    get() = layout.buildDirectory.asFile.get()

internal val Project.stonecutterCacheDir
    get() = buildDirectory.resolve("stonecutter-cache")

internal val Project.stonecutterCachePath
    get() = stonecutterCacheDir.toPath()
