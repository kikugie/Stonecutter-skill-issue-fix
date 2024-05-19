package dev.kikugie.stonecutter.version

import dev.kikugie.stonecutter.metadata.Semver
import java.util.function.Predicate

typealias Version = Any

interface VersionChecker {
    fun parseVersion(version: Semver): Version
    fun parsePredicate(predicate: String): Predicate<Version>
    fun test(version: Version, predicate: String): Boolean? = try {
        parsePredicate(predicate).test(version)
    } catch (e: Exception) {
        null
    }
}