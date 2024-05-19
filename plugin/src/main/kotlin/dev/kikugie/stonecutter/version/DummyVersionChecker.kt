package dev.kikugie.stonecutter.version

import java.util.function.Predicate

object DummyVersionChecker : VersionChecker {
    override fun parseVersion(version: String) = version

    override fun parsePredicate(predicate: String): Predicate<Version> {
        throw UnsupportedOperationException()
    }
}