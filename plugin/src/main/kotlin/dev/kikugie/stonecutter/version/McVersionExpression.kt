package dev.kikugie.stonecutter.version

import dev.kikugie.stitcher.process.access.Expression

class McVersionExpression(private val current: Version, private val checker: VersionChecker) : Expression {
    override fun invoke(predicate: String): Boolean? = checker.test(current, predicate)
}