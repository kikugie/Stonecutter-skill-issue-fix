package dev.kikugie.stonecutter

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import dev.kikugie.stonecutter.data.tree.TreeModel
import java.nio.file.Path

/**
 * Used as a return value by some configuration methods.
 * Generic type erasure makes `func(iter: Iterable<A>)` and `func(iter: Iterable<B>)`
 * have conflicting signatures on the JVM.
 */
public const val BNAN: String = "🍌"

// Updated by the 'updateVersion' task
/**
 * Currently running Stonecutter version, serialised in [TreeModel].
 */
public const val STONECUTTER: String = "0.6-alpha.1"

internal operator fun <K, V> Map<K, V>?.get(key: K): V? = this?.get(key)
internal fun <K : Any, R : Any> memoize(memory: (K) -> R?): (K) -> R? = mutableMapOf<K, R?>().let { map ->
    { key -> map.getOrPut(key) { memory(key) } }
}

internal fun String.removeStarting(char: Char): String {
    var index = 0
    while (index < length && get(index) == char) index++
    return substring(index)
}

internal val Project.projectPath: Path get() = projectDir.toPath()
internal val Project.buildDirectoryFile
    get() = layout.buildDirectory.asFile.get()

internal val Project.buildDirectoryPath
    get() = layout.buildDirectory.asFile.get().toPath()

internal val Project.stonecutterCacheFile
    get() = buildDirectoryFile.resolve("stonecutter-cache")

internal val Project.stonecutterCachePath
    get() = stonecutterCacheFile.toPath()

internal infix fun Path.cut(other: Path): Path = relativize(other)
internal operator fun <T> Provider<T>.invoke() = get()
internal operator fun <T> Property<T>.invoke(value: T) = set(value)

internal inline fun <T> Iterable<T>.onEach(action: T.() -> Unit) = forEach(action)
internal infix fun <T> Any?.then(other: T): T = other