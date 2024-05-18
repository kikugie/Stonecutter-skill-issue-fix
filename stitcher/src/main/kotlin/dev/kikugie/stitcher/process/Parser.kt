package dev.kikugie.stitcher.process

import dev.kikugie.stitcher.data.*
import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.exception.StitcherThrowable
import dev.kikugie.stitcher.type.Comment.*
import dev.kikugie.stitcher.type.NULL
import dev.kikugie.stitcher.type.StitcherToken.*
import dev.kikugie.stitcher.type.TokenType
import java.util.*

class Parser(input: Iterable<Token>) {
    companion object {
        const val VERSION = 1
        fun Sequence<Token>.parse() = Parser(this.asIterable()).parse()
    }

    private val iter = input.lookaround()
    private val scopeStack = Stack<Scope>()
    private val rootScope = RootScope(VERSION)

    private val active
        get() = if (scopeStack.empty()) rootScope else scopeStack.peek()

    fun parse(): RootScope {
        while (iter.hasNext()) process()
        return rootScope
    }

    private fun process() {
        if (match(CONTENT))
            active.add(ContentBlock(iter.next()))
        else if (match(COMMENT_START))
            matchComment().also { return }

        if (active.enclosure != ScopeType.CLOSED)
            scopeStack.pop()
    }

    private fun matchComment() {
        val start = consume()
        when (match(CONDITION, SWAP)) {
            CONDITION -> matchCondition(start)
            SWAP -> matchSwap(start)
            else -> {
                val contents = consume(COMMENT, "Expected comment contents")
                val end = getOrCreateCommentEnd()
                val comment = CommentBlock(start, Literal(contents), end)
                active.add(comment)
            }
        }
    }

    private fun matchCondition(start: Token) {
        skip() // Skip ?
        var extension = false
        if (match(SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != CONDITION) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            if (match(COMMENT_END, NULL) != null) {
                val condition = Condition(extension = true)
                val comment = CommentBlock(start, condition, getOrCreateCommentEnd())
                scopeStack.pop()
                active.add(comment)
                return
            }

            scopeStack.pop()
            extension = true
        }

        val sugar = createConditionSugar()
        val hasExpression = match(EXPRESSION)
        val expression = if (!hasExpression && sugar.lastOrNull()?.type == ELSE)
            Empty else matchExpression()
        val scope = createScope(CONDITION)
        val condition = Condition(sugar, expression, extension)
        val comment = CommentBlock(start, condition, getOrCreateCommentEnd(), scope)
        active.add(comment)
        scopeStack.push(scope)
    }

    private fun matchSwap(start: Token) {
        skip() // Skip $
        if (match(SCOPE_CLOSE)) {
            if (scopeStack.empty()) throw StitcherSyntaxException(iter.peek!!, "No scope to close")
            if (active.type != SWAP) throw StitcherSyntaxException(
                iter.peek!!,
                "Closes a block of different type ${active.type}"
            )
            skip() // Skip }
            if (match(COMMENT_END, NULL) != null) {
                val swap = Swap(extension = true)
                val comment = CommentBlock(start, swap, getOrCreateCommentEnd())
                active.add(comment)
            }
            scopeStack.pop()
        } else {
            val id = consume(
                EXPRESSION,
                "Expected an identifier after $. To disable this swap block use `null`"
            )
            val scope = createScope(SWAP)
            val swap = Swap(id)
            val comment = CommentBlock(start, swap, getOrCreateCommentEnd(), scope)
            active.add(comment)
            scopeStack.push(scope)
        }
    }

    private fun matchExpression(): Component = when {
        match(EXPRESSION) -> {
            val left = Literal(consume())
            if (match(OR, AND) != null) {
                val operator = consume()
                val right = matchExpression()
                Binary(left, operator, right)
            } else left
        }

        match(NEGATE) -> {
            val operator = consume()
            val target = matchExpression()
            Unary(operator, target)
        }

        match(GROUP_OPEN) -> {
            skip()
            val group = Group(matchExpression())
            consume(GROUP_CLOSE, "Expected `)` to close the group")
            if (match(OR, AND) != null) {
                val operator = consume()
                val right = matchExpression()
                Binary(group, operator, right)
            } else group
        }

        else -> throw IllegalArgumentException("Unexpected token: ${iter.peek}")
    }

    private fun createConditionSugar(): List<Token> = buildList {
        while (
            iter.peek?.type == IF ||
            iter.peek?.type == ELSE ||
            iter.peek?.type == ELIF
        ) {
            add(iter.next())
        }
    }

    private fun createScope(type: TokenType): Scope {
        val scopeType = when (match(SCOPE_OPEN, EXPECT_WORD)) {
            SCOPE_OPEN -> ScopeType.CLOSED
            EXPECT_WORD -> ScopeType.WORD
            else -> ScopeType.LINE
        }
        if (scopeType != ScopeType.LINE) skip() // Skip { or >>
        return Scope(type, scopeType)
    }

    private fun match(type: TokenType): Boolean =
        iter.peek?.type == type

    private fun match(vararg types: TokenType): TokenType? =
        types.firstOrNull { iter.peek?.type == it }

    private fun consume() = iter.next()

    private fun consume(type: TokenType, message: String, strict: Boolean = true): Token {
        if (match(type)) return iter.next()
        else throw StitcherThrowable.create(iter.peek ?: Token.EOF, message, strict)
    }

    private fun getOrCreateCommentEnd(): Token = when {
        match(COMMENT_END) -> iter.next()
        match(NULL) -> Token("", COMMENT_END)
        else -> throw StitcherSyntaxException(iter.peek!!, "Expected the comment to end")
    }

    private fun skip() {
        iter.next()
    }

    private fun <T> Iterable<T>.lookaround(): LookaroundIterator<T> = LookaroundIterator(iterator())

    private class LookaroundIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {
        var current: T? = null
            private set
        var peek: T? = null
            private set
        var prev: T? = null
            private set

        init {
            if (iterator.hasNext()) {
                peek = iterator.next()
            }
        }

        override fun hasNext() = iterator.hasNext()

        override fun next(): T {
            prev = current
            current = peek
            peek = if (iterator.hasNext()) iterator.next() else null

            return current ?: throw NoSuchElementException("No more elements present.")
        }
    }
}