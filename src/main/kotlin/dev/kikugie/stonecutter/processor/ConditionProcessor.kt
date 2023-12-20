package dev.kikugie.stonecutter.processor

typealias Expression = (String) -> Boolean?

data class ConditionProcessor(private val checkers: Iterable<Expression>) {
    fun test(expr: String): Boolean {
        for (it in checkers) return it(expr) ?: continue
        throw IllegalArgumentException()
    }

    companion object {
        val TRUE: Expression = { if (it == "true") true else null }
        val FALSE: Expression = { if (it == "false") false else null }
        val TEST = ConditionProcessor(listOf(TRUE, FALSE))
    }
}