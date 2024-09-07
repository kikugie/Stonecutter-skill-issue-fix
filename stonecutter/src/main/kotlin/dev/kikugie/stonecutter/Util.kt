package dev.kikugie.stonecutter

import org.gradle.api.Project
import java.io.Closeable
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile

typealias StonecutterSettings = dev.kikugie.stonecutter.settings.StonecutterSettings

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

internal inline fun <T : Closeable?, R> T.useCatching(block: (T) -> R): Result<R> =
    runCatching { use(block) }

internal fun Path.isAvailable() = exists() && isRegularFile() && isReadable()

/**
 * Delegates set operation. Meant to be used with Kotlin DSL.
 *
 * @param K Key type
 * @param V Value type
 */
interface MapSetter<K, V> {
    operator fun set(key: K, value: V)
}