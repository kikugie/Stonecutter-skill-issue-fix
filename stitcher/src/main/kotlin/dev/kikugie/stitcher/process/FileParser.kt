package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.recognizer.CommentRecognizer
import dev.kikugie.stitcher.process.util.LookaroundIterator
import java.io.Reader
import java.util.*

class FileParser(input: Sequence<Token>, private val handler: ErrorHandler) {
    constructor(input: Reader, handler: ErrorHandler, recognizers: Iterable<CommentRecognizer>) : this(
        input.scan(
            recognizers
        ), handler
    )

    private val iter = LookaroundIterator(input.iterator())
    private val scopes = Stack<Scope>()
        .apply { push(Scope()) }
    private val active get() = scopes.peek()
    private val root get() = scopes[0]

    private fun add(block: Block) {
        active.add(block)
    }

    private val commentStart get() = iter.prev!!
    private val commentEnd
        get() = when (iter.peek?.type) {
            ContentType.COMMENT_END -> iter.peek!!
            null -> Token("", ContentType.COMMENT_END)
            else -> throw AssertionError()
        }

    fun parse() = iter.forEach {
        when (it.type) {
            ContentType.CONTENT -> add(ContentBlock(it))
            ContentType.COMMENT -> parseComment(it)
            ContentType.COMMENT_START, ContentType.COMMENT_END -> {}
            else -> throw AssertionError()
        }
        if (active.enclosure != ScopeType.CLOSED && active.lastOrNull()?.isNotEmpty() == true)
            scopes.pop()
    }.let { root }

    private fun createParser(str: CharSequence): CommentParser {
        val handler = ErrorHandlerImpl(str)
        val lexer = Lexer(str, handler)
        return CommentParser(lexer, handler)
    }

    private fun parseComment(token: Token) {
        val parser = createParser(token.value)
        val def = parser.parse() ?: run {
            add(CommentBlock(commentStart, token, commentEnd))
            return
        }
        parser.errors.forEach { handler.addSilent(it.first, it.second) }
        if (def.extension)
            if (def.type == active.type) scopes.pop()
            else handler.accept(0, "${def.type} closes unmatched scope of ${active.type}")
        val scope = if (def.isEmpty() && def.extension) null else Scope(def.type, def.enclosure)
        add(CodeBlock(commentStart, def, commentEnd, scope))
        if (scope != null) scopes.push(scope)
    }
}