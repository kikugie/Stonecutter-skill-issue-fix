package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.Syntax
import dev.kikugie.stitcher.type.StitcherToken.*
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitcher.process.recognizer.TokenRecognizer
import dev.kikugie.stitcher.type.TokenType
import dev.kikugie.stitcher.util.leadingSpaces
import dev.kikugie.stitcher.util.trailingSpaces

class Lexer(private val input: Iterable<Token>) {
    fun tokenize(): Sequence<Token> = sequence {
        input.forEach { process(it) }
    }

    private suspend fun SequenceScope<Token>.process(token: Token) {
        if (token.type != Comment.COMMENT) {
            yield(token)
            return
        }

        when (token.value.firstOrNull()) {
            '?' -> {
                yield(token.take(0..<1, CONDITION))
                yieldAll(scanContents(token, Syntax.conditionState))
            }

            '$' -> {
                yield(token.take(0..<1, SWAP))
                yieldAll(scanContents(token, Syntax.swapState))
            }

            else -> yield(token)
        }
    }

    private fun scanContents(token: Token, checkers: List<Pair<TokenType, TokenRecognizer>>): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 1
        val buffer = StringBuilder()

        fun expressionToken() {
            if (buffer.isNotBlank()) tokens += token.take(
                index - buffer.length + buffer.leadingSpaces()..<index - buffer.trailingSpaces(),
                EXPRESSION
            )
        }

        while (index < token.value.length) {
            var matched = false
            for ((type, it) in checkers) {
                val result = it.recognize(token.value, index) ?: continue
                expressionToken()
                if (buffer.isNotEmpty()) buffer.clear()

                tokens += token.take(result.range, type)
                index = result.range.last + 1
                matched = true
                break
            }
            if (!matched) buffer.append(token.value[index++])
        }
        expressionToken()
        return tokens
    }

    companion object {
        fun Sequence<Token>.lex() = Lexer(asIterable()).tokenize()
    }
}