package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.exception.accept
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.scanner.CommentRecognizer
import dev.kikugie.stitcher.scanner.Scanner.Companion.scan
import dev.kikugie.stitcher.transformer.TransformParameters
import java.io.Reader
import java.util.*

/**
 * Parser for the entire file contents.
 *
 * @property handlerFactory Exception collector function for each comment
 * @constructor
 * Creates parser from a reader, directly running the [Scanner]
 *
 * @param input Sequence of tokens produced by the scanner or a reader to be scanned
 */
class FileParser(
    input: Sequence<Token>,
    private val params: TransformParameters? = null,
    private val handlerFactory: (CharSequence) -> ErrorHandler = ::ErrorHandlerImpl,
) {
    constructor(
        input: Reader,
        recognizers: Iterable<CommentRecognizer>,
        params: TransformParameters? = null,
        handlerFactory: (CharSequence) -> ErrorHandler = ::ErrorHandlerImpl,
    ) : this(input.scan(recognizers), params, handlerFactory)

    private val errors = mutableListOf<Throwable>()
    private val iter = LookaroundIterator(input.iterator())
    private val scopes = Stack<Scope>()
        .apply { push(Scope()) }
    private val active get() = scopes.peek()
    private val root get() = scopes[0]
    val errs: List<Throwable> get() = errors

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

    /**
     * Parses the input sequence, creating an AST of scopes and blocks.
     *
     * @return Root scope of [NullType], which cannot be closed
     */
    fun parse(): Scope = iter.forEach {
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
        val handler = handlerFactory(str)
        val lexer = Lexer(str, handler)
        return CommentParser(lexer, handler, params)
    }

    private fun parseComment(token: Token) {
        val parser = createParser(token.value)
        val def = parser.parse() ?: run {
            add(CommentBlock(commentStart, token, commentEnd))
            return
        }
        if (def.extension)
            if (def.type == active.type) scopes.pop()
            else parser.handler.accept(1, "${def.type} closes unmatched scope of ${active.type}")
        val scope = if (def.isEmpty() && def.extension) null else Scope(def.type, def.enclosure)
        add(CodeBlock(commentStart, def, commentEnd, scope))
        if (scope != null) scopes.push(scope)
        parser.errors.forEach { errors.add(it.second) }
    }
}