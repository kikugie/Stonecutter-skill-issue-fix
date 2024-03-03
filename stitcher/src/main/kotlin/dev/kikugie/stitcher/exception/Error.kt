package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.token.Token

sealed interface StitcherThrowable {
    companion object {
        fun yeet(token: Token, reason: String, strict: Boolean = true): RuntimeException = if (strict)
            StitcherSyntaxException(token, reason) else StitcherSyntaxWarning(token, reason)
    }
}

class StitcherSyntaxException(val token: Token, val reason: String) : RuntimeException(reason), StitcherThrowable

class StitcherSyntaxWarning(val token: Token, val reason: String) : RuntimeException(reason), StitcherThrowable