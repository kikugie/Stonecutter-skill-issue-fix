package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.ContentType
import dev.kikugie.stitcher.data.token.NullType
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.eval.isEmpty
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stitcher.exception.ErrorHandler
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.ALL
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.transformer.TransformParameters
import java.util.*

/**
 * Parser for the entire file contents.
 *
 * @property handlerFactory Exception collector function for each comment
 *
 * @param input Sequence of tokens produced by the scanner or a reader to be scanned
 */
class FileParser(
    input: Sequence<Token>,
    private val params: TransformParameters? = null,
    private val handlerFactory: () -> ErrorHandler = ::StoringErrorHandler,
) {
    private val iter: LookaroundIterator<Token> = LookaroundIterator(input.iterator())
    private val scopes: Stack<Scope> = Stack<Scope>().apply { push(Scope()) }
    private val active: Scope get() = scopes.peek()
    private val root: Scope get() = scopes[0]
    private val handlers: MutableList<ErrorHandler> = mutableListOf()
    val hasErrors: Boolean get() = handlers.any { it.errors.isNotEmpty() }
    val errors: Sequence<Pair<LexSlice, String>> get() = handlers.asSequence().flatMap { it.errors }

    private val commentStart get() = iter.prev!!
    private val commentEnd get() = when (iter.peek?.type) {
            ContentType.COMMENT_END -> iter.peek!!
            null, NullType -> Token("", ContentType.COMMENT_END)
            else -> throw AssertionError("Unexpected token: ${iter.peek}")
        }

    /**
     * Parses the input sequence, creating an AST of scopes and blocks.
     *
     * @return Root scope of [NullType], which cannot be closed
     */
    fun parse(): Scope = iter.forEach {
        when (it.type) {
            ContentType.CONTENT -> active.blocks.add(ContentBlock(it))
            ContentType.COMMENT -> parseComment(it)
            ContentType.COMMENT_START, ContentType.COMMENT_END -> {}
            else -> throw AssertionError("Unexpected token: $it")
        }
        if (active.enclosure != ScopeType.CLOSED && active.blocks.lastOrNull()?.isNotEmpty() == true)
            scopes.pop()
    }.let { root }

    private fun parseComment(token: Token) {
        val handler = handlerFactory()
        val lexer = Lexer(token.value, ALL, handler)
        val parser = CommentParser(lexer, handler, params)
        val def = parser.parse() ?: run {
            active.blocks.add(CommentBlock(commentStart, token, commentEnd))
            return
        }

        if (def.extension)
            if (def.type == active.type) scopes.pop()
            else handler.accept(lexer[1]!!, "${def.type} closes unmatched scope of ${active.type}")

        val scope = if (def.isEmpty() && def.extension && def.enclosure == ScopeType.LINE) null
        else Scope(def.type, def.enclosure)
        active.blocks.add(CodeBlock(commentStart, def, commentEnd, scope))
        if (scope != null) scopes.push(scope)
    }
}