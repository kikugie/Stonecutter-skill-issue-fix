package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.exception.StitcherThrowable
import dev.kikugie.stitcher.lexer.StitcherTokenType
import dev.kikugie.stitcher.scanner.CommentType
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.token.TokenType
import dev.kikugie.stitcher.util.LookaroundIterator.Companion.lookaround
import java.util.*

class Parser(input: Iterable<Token>) {
    private val iter = input.iterator().lookaround()
    private val scopeStack = Stack<Scope>()
    val rootScope = Scope()

    private val active
        get() = if (scopeStack.empty()) rootScope else scopeStack.peek()

    fun parse() {
        while (iter.hasNext()) process()
    }

    private fun process() {
        if (match(CommentType.CONTENT))
            active.add(ContentBlock(iter.next()))
        else if (match(CommentType.COMMENT_START))
            matchComment()

        if (active.enclosure != ScopeType.CLOSED)
            scopeStack.pop()
    }

    private fun matchComment() {
        val start = consume()
        when (match(StitcherTokenType.CONDITION, StitcherTokenType.SWAP)) {
            StitcherTokenType.CONDITION -> matchCondition(start)
            StitcherTokenType.SWAP -> matchSwap(start)
            else -> {
                val contents = consume(CommentType.COMMENT, "Expected comment contents")
                val end = consume(CommentType.COMMENT_END, "Expected comment to end")
                val comment = CommentBlock(start, Literal(contents), end)
                active.add(comment)
            }
        }
    }

    private fun matchCondition(start: Token) {
        skip() // Skip ?
        var extension = false
        if (match(StitcherTokenType.SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != StitcherTokenType.CONDITION) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            scopeStack.pop()
            if (match(CommentType.COMMENT_END)) {
                skip()
                return
            }

            extension = true
        }

        val sugar = createConditionSugar(extension)

        // TODO: grouping and binary operations
        // FIXME: plain else doesnt work
        val hasExpression = match(StitcherTokenType.EXPRESSION)
        val expression = if (!hasExpression && sugar.lastOrNull()?.type == StitcherTokenType.ELSE)
            Empty else Literal(
            consume(
                StitcherTokenType.EXPRESSION,
                "Expected an expression. To get a constant behaviour use `true` or `false`"
            )
        )
        val scope = createScope(StitcherTokenType.CONDITION)
        val condition = Condition(sugar, expression, extension)
        val comment = CommentBlock(start, condition, consume(CommentType.COMMENT_END, "Expected the comment to end"), scope)
        active.add(comment)
        scopeStack.push(scope)
    }

    private fun createConditionSugar(isClosed: Boolean): List<Token> {
        val sugar = mutableListOf<Token>()
        val hasElse = match(StitcherTokenType.ELSE)
//        val previous = active.blocks.last()

        // TODO: Check if its valid in relation to the previous comment. I'm too tired rn for this
        if (!isClosed && hasElse)
            throw StitcherSyntaxException(iter.peek!!, "Invalid token. Check if previous condition is closed with `}`")
        if (hasElse) {
            sugar.add(iter.next())
        }
        if (match(StitcherTokenType.IF))
            sugar.add(iter.next())
        return sugar
    }

    private fun matchSwap(start: Token) {
        skip() // Skip $
        if (match(StitcherTokenType.SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != StitcherTokenType.SWAP) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            consume(CommentType.COMMENT_END, "Expected the comment to end")
            scopeStack.pop()
        } else {
            val id = consume(
                StitcherTokenType.EXPRESSION,
                "Expected an identifier after $. To disable this swap block use `null`"
            )
            val scope = createScope(StitcherTokenType.SWAP)
            val swap = Swap(id)
            val comment = CommentBlock(start, swap, consume(CommentType.COMMENT_END, "Expected the comment to end"), scope)
            active.add(comment)
            scopeStack.push(scope)
        }
    }

    private fun createScope(type: StitcherTokenType): Scope {
        val isClosedScope = match(StitcherTokenType.SCOPE_OPEN)
        val scopeType = if (isClosedScope) ScopeType.CLOSED else ScopeType.LINE
        if (isClosedScope) skip() // Skip {
        return Scope(type, scopeType)
    }

    private fun match(type: TokenType): Boolean =
        iter.peek?.type == type

    private fun match(vararg types: TokenType): TokenType? =
        types.firstOrNull { iter.peek?.type == it }

    private fun consume() = iter.next()

    private fun consume(type: TokenType, message: String, strict: Boolean = true): Token {
        if (match(type)) return iter.next()
        else
            throw StitcherThrowable.create(iter.peek ?: Token.eof(iter.current!!.range.last + 1), message, strict)
    }

    private fun skip() {
        iter.next()
    }
}