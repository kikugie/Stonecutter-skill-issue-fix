package dev.kikugie.stonecutter.version

import dev.kikugie.stonecutter.processor.Expression

class McVersionExpression(val current: Version, val checker: VersionChecker) : Expression {
    override fun invoke(predicate: String): Boolean? = checker.test(current, predicate)
}