package dev.kikugie.stonecutter.version

import dev.kikugie.stitcher.process.access.Expression
import dev.kikugie.stonecutter.metadata.Semver

class DependencyChecker(private val dependencies: Map<String, Semver>, private val checker: VersionChecker) : Expression {
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun invoke(predicate: String): Boolean {
        val (id, condition) = predicate.split(':', limit = 2).let {
            if (it.size == 1) "minecraft" to it[0].trim()
            else (if (it[0].isBlank()) "minecraft" else it[0].trim()) to it[1].trim()
        }
        val version = dependencies[id]
            ?: throw IllegalArgumentException("""
                [Stonecutter] Dependency $id is not registered. 
                Add it using `stonecutter.dependency()`.""".trimIndent())
        val semver = try {
            checker.parseVersion(version)
        } catch (_: Exception) {
            throw IllegalArgumentException("""
                [Stonecutter] Version $version registered for $id is invalid. 
                Change it to the Semver format.""".trimIndent())
        }
        return checker.test(semver, condition)
            ?: throw IllegalStateException("""
                [Stonecutter] Failed to parse predicate $condition. 
                This is likely caused by a missing Fabric Loader dependency. 
                If this is not a Fabric mod, add the loader as compile only dependency.""".trimIndent())
    }
}