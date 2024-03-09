package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.StitcherToken

typealias Expression = (String) -> Boolean?

@Suppress("MemberVisibilityCanBePrivate")
data class ExpressionProcessor(private val checkers: Iterable<Expression>) {
    private val cache = mutableMapOf<String, Boolean?>()

    fun test(expr: String): Boolean {
        cache[expr]?.run { return this }
        for (it in checkers) return it(expr).also { cache[expr] = it } ?: continue
        throw AssertionError()
    }

    internal fun test(token: Token): Boolean {
        if (token.type != StitcherToken.EXPRESSION)
            throw StitcherSyntaxException(token, "Only EXPRESSION tokens can be tested")
        return try {
            test(token.value)
        } catch (e: AssertionError) {
            throw StitcherSyntaxException(token, "Invalid expression")
        }
    }
}