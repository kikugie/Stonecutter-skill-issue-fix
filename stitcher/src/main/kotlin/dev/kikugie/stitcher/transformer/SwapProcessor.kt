package dev.kikugie.stitcher.transformer

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.StitcherToken


class SwapProcessor(
    private val swaps: Map<String, (ExpressionProcessor) -> String>,
    private val checker: ExpressionProcessor
) {
    fun get(token: Token): String {
        if (token.type != StitcherToken.EXPRESSION)
            throw StitcherSyntaxException(token, "Only EXPRESSION tokens can be tested")
        return swaps[token.value]?.invoke(checker) ?:
            throw StitcherSyntaxException(token, "Swap token is not registered")
    }

    class Builder {
        private val swaps = mutableMapOf<String, (ExpressionProcessor) -> String>()

        operator fun set(token: String, operation: (ExpressionProcessor) -> String) {
            // TODO: throw for duplicate tokens
            swaps[token] = operation
        }

        internal fun build(checker: ExpressionProcessor) = SwapProcessor(swaps, checker)
    }
}