package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.exception.StitcherThrowable
import dev.kikugie.stitcher.type.StitcherToken
import dev.kikugie.stitcher.type.Comment
import dev.kikugie.stitcher.type.NULL
import dev.kikugie.stitcher.token.Token
import dev.kikugie.stitcher.type.TokenType
import dev.kikugie.stitcher.util.LookaroundIterator.Companion.lookaround
import java.util.*

class Parser(input: Iterable<Token>) {
    companion object {
        const val VERSION = 1
    }

    private val iter = input.iterator().lookaround()
    private val scopeStack = Stack<Scope>()
    val rootScope = RootScope(VERSION)

    private val active
        get() = if (scopeStack.empty()) rootScope else scopeStack.peek()

    fun parse(): RootScope {
        while (iter.hasNext()) process()
        return rootScope
    }

    private fun process() {
        if (match(Comment.CONTENT))
            active.add(ContentBlock(iter.next()))
        else if (match(Comment.COMMENT_START))
            matchComment()

        if (active.enclosure != ScopeType.CLOSED)
            scopeStack.pop()
    }

    private fun matchComment() {
        val start = consume()
        when (match(StitcherToken.CONDITION, StitcherToken.SWAP)) {
            StitcherToken.CONDITION -> matchCondition(start)
            StitcherToken.SWAP -> matchSwap(start)
            else -> {
                val contents = consume(Comment.COMMENT, "Expected comment contents")
                val end = getOrCreateCommentEnd()
                val comment = CommentBlock(start, Literal(contents), end)
                active.add(comment)
            }
        }
    }

    private fun matchCondition(start: Token) {
        skip() // Skip ?
        var extension = false
        if (match(StitcherToken.SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != StitcherToken.CONDITION) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            scopeStack.pop()
            if (match(Comment.COMMENT_END)) {
                skip()
                return
            }

            extension = true
        }

        val sugar = createConditionSugar(extension)

        val hasExpression = match(StitcherToken.EXPRESSION)
        val expression = if (!hasExpression && sugar.lastOrNull()?.type == StitcherToken.ELSE)
            Empty else matchExpression()
        val scope = createScope(StitcherToken.CONDITION)
        val condition = Condition(sugar, expression, extension)
        val comment =
            CommentBlock(start, condition, getOrCreateCommentEnd(), scope)
        active.add(comment)
        scopeStack.push(scope)
    }

    private fun matchExpression(grouped: Boolean = false): Component = when {
        match(StitcherToken.EXPRESSION) -> {
            val left = Literal(consume())
            if (match(StitcherToken.OR, StitcherToken.AND) != null) {
                val operator = consume()
                val right = matchExpression()
                Binary(left, operator, right)
            } else left
        }

        match(StitcherToken.NEGATE) -> {
            val operator = consume()
            val target = matchExpression()
            Unary(operator, target)
        }

        match(StitcherToken.GROUP_OPEN) -> {
            skip()
            val group = Group(matchExpression(true))
            consume(StitcherToken.GROUP_CLOSE, "Expected `)` to close the group")
            if (match(StitcherToken.OR, StitcherToken.AND) != null) {
                val operator = consume()
                val right = matchExpression(true)
                Binary(group, operator, right)
            } else group
        }

        else -> throw StitcherSyntaxException(iter.peek!!, "Unexpected token")
    }

    private fun createConditionSugar(isClosed: Boolean): List<Token> {
        val sugar = mutableListOf<Token>()
        val hasElse = match(StitcherToken.ELSE)
//        val previous = active.blocks.last()

        // TODO: Check if its valid in relation to the previous comment. I'm too tired rn for this
        if (!isClosed && hasElse)
            throw StitcherSyntaxException(iter.peek!!, "Invalid token. Check if previous condition is closed with `}`")
        if (hasElse) {
            sugar.add(iter.next())
        }
        if (match(StitcherToken.IF))
            sugar.add(iter.next())
        return sugar
    }

    private fun matchSwap(start: Token) {
        skip() // Skip $
        if (match(StitcherToken.SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != StitcherToken.SWAP) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            getOrCreateCommentEnd()
            scopeStack.pop()
        } else {
            val id = consume(
                StitcherToken.EXPRESSION,
                "Expected an identifier after $. To disable this swap block use `null`"
            )
            val scope = createScope(StitcherToken.SWAP)
            val swap = Swap(id)
            val comment =
                CommentBlock(start, swap, getOrCreateCommentEnd(), scope)
            active.add(comment)
            scopeStack.push(scope)
        }
    }

    private fun createScope(type: StitcherToken): Scope {
        val isClosedScope = match(StitcherToken.SCOPE_OPEN)
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
        else throw StitcherThrowable.create(iter.peek ?: Token.eof(iter.current!!.range.last + 1), message, strict)
    }

    private fun getOrCreateCommentEnd(): Token = when {
        match(Comment.COMMENT_END) -> iter.next()
        match(NULL) -> Token("", iter.current!!.range.let { it.last + 1..<-1 }, Comment.COMMENT_END)
        else -> throw StitcherSyntaxException(iter.peek!!, "Expected the comment to end")
    }

    private fun skip() {
        iter.next()
    }
}