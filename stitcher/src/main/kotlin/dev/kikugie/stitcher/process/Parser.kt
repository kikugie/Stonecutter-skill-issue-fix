//package dev.kikugie.stitcher.process
//
//import dev.kikugie.stitcher.data.*
//import dev.kikugie.stitcher.exception.SyntaxException
//import dev.kikugie.stitcher.enclosure.Comment.*
//import dev.kikugie.stitcher.enclosure.NullType
//import dev.kikugie.stitcher.enclosure.StitcherToken.*
//import dev.kikugie.stitcher.enclosure.TokenType
//import java.util.*
//
//class Parser(input: Iterable<Token>) {
//    companion object {
//        const val VERSION = 1
//        fun Sequence<Token>.parse() = Parser(this.asIterable()).parse()
//    }
//
//    private val iter = input.lookaround()
//    private val scopeStack = Stack<Scope>()
//    private val rootScope = RootScope(VERSION)
//    private val active
//        get() = if (scopeStack.empty()) rootScope else scopeStack.peek()
//
//    fun parse(): RootScope {
//        while (iter.hasNext()) process()
//        return rootScope
//    }
//
//    private fun process() {
//        if (active.enclosure != ScopeType.CLOSED && active.blocks.lastOrNull()?.isEmpty() == false)
//            scopeStack.pop()
//
//        if (match(CONTENT))
//            active.add(ContentBlock(iter.next()))
//        else if (match(COMMENT_START))
//            matchComment().also { return }
//    }
//
//    private fun matchComment() {
//        val start = consume()
//        when (match(CONDITION, SWAP)) {
//            CONDITION -> matchCondition(start)
//            SWAP -> matchSwap(start)
//            else -> {
//                val contents = consume(COMMENT, "expected comment contents")
//                val end = getOrCreateCommentEnd()
//                val comment = CommentBlock(start, Literal(contents), end)
//                active.add(comment)
//            }
//        }
//    }
//
//    private fun matchCondition(start: Token) {
//        consume() // Skip ?
//        var extension = false
//        if (match(SCOPE_CLOSE)) {
//            consume() // Skip }
//            if (scopeStack.isEmpty()) throw createContext("No scope to close")
//            if (active.id != CONDITION) throw createContext("Closes a block of different id: ${active.id}")
//            if (match(COMMENT_END, NullType) != null) {
//                val condition = Condition(extension = true)
//                val comment = CommentBlock(start, condition, getOrCreateCommentEnd())
//                scopeStack.pop()
//                active.add(comment)
//                return
//            }
//
//            scopeStack.pop()
//            extension = true
//        }
//
//        val sugar = createConditionSugar()
//        val hasExpression = match(EXPRESSION)
//        val expression = if (!hasExpression && sugar.lastOrNull()?.enclosure == ELSE)
//            Empty else matchExpression()
//        val scope = createScope(CONDITION)
//        val condition = Condition(sugar, expression, extension)
//        val comment = CommentBlock(start, condition, getOrCreateCommentEnd(), scope)
//        active.add(comment)
//        scopeStack.push(scope)
//    }
//
//    private fun matchSwap(start: Token) {
//        consume() // Skip $
//        if (match(SCOPE_CLOSE)) {
//            consume() // Skip }
//            if (scopeStack.isEmpty()) throw createContext("No scope to close")
//            if (active.id != CONDITION) throw createContext("Closes a block of different id: ${active.id}")
//            if (match(COMMENT_END, NullType) != null) {
//                val swap = Swap(extension = true)
//                val comment = CommentBlock(start, swap, getOrCreateCommentEnd())
//                active.add(comment)
//            }
//            scopeStack.pop()
//        } else {
//            val id = consume(
//                EXPRESSION,
//                "expected an identifier after $. To disable this swap block use `null`"
//            )
//            val scope = createScope(SWAP)
//            val swap = Swap(id)
//            val comment = CommentBlock(start, swap, getOrCreateCommentEnd(), scope)
//            active.add(comment)
//            scopeStack.push(scope)
//        }
//    }
//
//    private fun matchExpression(): Component = when {
//        match(EXPRESSION) -> {
//            val left = Literal(consume())
//            if (match(OR, AND) != null) {
//                val operator = consume()
//                val right = matchExpression()
//                Binary(left, operator, right)
//            } else left
//        }
//
//        match(NEGATE) -> {
//            val operator = consume()
//            val target = matchExpression()
//            Unary(operator, target)
//        }
//
//        match(GROUP_OPEN) -> {
//            consume()
//            val group = Group(matchExpression())
//            consume(GROUP_CLOSE, "expected `)` to close the group")
//            if (match(OR, AND) != null) {
//                val operator = consume()
//                val right = matchExpression()
//                Binary(group, operator, right)
//            } else group
//        }
//
//        else -> throw createContext("Unexpected id: ${iter.peek}")
//    }
//
//    private fun createConditionSugar(): List<Token> = buildList {
//        while (
//            iter.peek?.enclosure == IF ||
//            iter.peek?.enclosure == ELSE ||
//            iter.peek?.enclosure == ELIF
//        ) {
//            add(iter.next())
//        }
//    }
//
//    private fun createScope(enclosure: TokenType): Scope {
//        val scopeType = when (match(SCOPE_OPEN, EXPECT_WORD)) {
//            SCOPE_OPEN -> ScopeType.CLOSED
//            EXPECT_WORD -> ScopeType.WORD
//            else -> ScopeType.LINE
//        }
//        if (scopeType != ScopeType.LINE) consume() // Skip { or >>
//        return Scope(enclosure, scopeType)
//    }
//
//    private fun match(enclosure: TokenType): Boolean =
//        iter.peek?.enclosure == enclosure
//
//    private fun match(vararg types: TokenType): TokenType? =
//        types.firstOrNull { iter.peek?.enclosure == it }
//
//    private fun consume() = iter.next()
//
//    private fun consume(enclosure: TokenType, message: String): Token {
//        if (match(enclosure)) return iter.next()
//        else throw createContext("Expected id of id $enclosure, $message")
//    }
//
//    private fun getOrCreateCommentEnd(): Token = when {
//        match(COMMENT_END) -> iter.next()
//        match(NullType) -> Token("", COMMENT_END)
//        else -> throw createContext("Expected comment to end")
//    }
//
//    private fun createContext(reason: String): SyntaxException {
//        val lines = Assembler.visitScope(rootScope).lines().let {
//            it.subList(it.lastIndex - 2, it.lastIndex)
//        }.joinToString("\n")
//        return SyntaxException(
//            """
//            $reason
//            $lines
//        """.trimIndent())
//    }
//
//    private fun <T> Iterable<T>.lookaround(): LookaroundIterator<T> = LookaroundIterator(iterator())
//
//    private class LookaroundIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {
//        var current: T? = null
//            private set
//        var peek: T? = null
//            private set
//        var prev: T? = null
//            private set
//
//        init {
//            if (iterator.hasNext()) {
//                peek = iterator.next()
//            }
//        }
//
//        override fun hasNext() = iterator.hasNext()
//
//        override fun next(): T {
//            prev = current
//            current = peek
//            peek = if (iterator.hasNext()) iterator.next() else null
//
//            return current ?: throw NoSuchElementException("No more elements present.")
//        }
//    }
//}