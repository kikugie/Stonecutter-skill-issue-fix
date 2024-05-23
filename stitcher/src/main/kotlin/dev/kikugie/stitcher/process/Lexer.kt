package dev.kikugie.stitcher.process

import Syntax
import dev.kikugie.stitcher.data.Token
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.process.util.LexSlice
import dev.kikugie.stitcher.process.util.locateToken
import dev.kikugie.stitcher.data.ContentType
import dev.kikugie.stitcher.data.MarkerType.*

class Lexer(val buffer: CharSequence, val handler: ErrorHandler) {
    val errors get() = handler.errors
    private val tokens = mutableListOf<LexSlice>()
    private var start = 0
    private var index = -1

    operator fun get(index: Int) = tokens.getOrNull(if (index >= 0) index else tokens.size + index)
    fun token(slice: LexSlice = tokens[index]) = Token(buffer.substring(slice.range), slice.type)
    fun advance(): LexSlice? = tokens.getOrNull(++index) ?: advanceInternal()

    fun lookup(offset: Int = 0): LexSlice? = when {
        offset > 0 -> tokens.getOrNull(index + offset) ?: run {
            if (start == -1) return@run null
            for (i in tokens.size..index + offset)
                advanceInternal()
            tokens.getOrNull(index + offset)
        }

        else -> tokens.getOrNull(index + offset)
    }

    private fun advanceInternal(): LexSlice? = when (start) {
        -1 -> null
        0 -> when (buffer.firstOrNull()) {
            '?' -> {
                ++start
                LexSlice(CONDITION, 0..0)
            }

            '$' -> {
                ++start
                LexSlice(SWAP, 0..0)
            }

            else -> {
                start = -1
                LexSlice(ContentType.COMMENT, buffer.indices)
            }
        }

        else -> nextToken()
    }?.also { tokens += it }

    private fun nextToken(): LexSlice? =
        locateToken(buffer, start, Syntax.ALL, handler).also {
            start = if (it == null) -1
            else it.second.last + 1
        }?.let { LexSlice(it.first, it.second) }
}