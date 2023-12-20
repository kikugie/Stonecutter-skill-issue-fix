package dev.kikugie.stonecutter.processor

enum class ExpressionType {
    SINGLE,
    OPENER,
    EXTENSION,
    CLOSER;

    companion object {
        fun of(expression: String): Pair<String, ExpressionType> {
            val opener = expression.endsWith('{')
            val closer = expression.startsWith('}')
            val formatted = expression.trim('{', '}', ' ')

            return if (opener && closer) formatted to EXTENSION
            else if (opener) formatted to OPENER
            else if (closer) formatted to CLOSER
            else formatted to SINGLE
        }
    }
}