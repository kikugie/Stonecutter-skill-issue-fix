package dev.kikugie.stonecutter.processor

import dev.kikugie.stonecutter.version.Version
import dev.kikugie.stonecutter.version.VersionChecker

class McVersionExpression(val current: Version, val checker: VersionChecker) : Expression {
    override fun invoke(predicate: String): Boolean? = checker.test(current, predicate)
}