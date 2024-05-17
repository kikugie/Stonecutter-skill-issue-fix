package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.data.Token

sealed interface StitcherThrowable {
    companion object {
        fun create(token: Token, reason: String, strict: Boolean = true): RuntimeException = if (strict)
            StitcherSyntaxException(token, reason) else StitcherSyntaxWarning(token, reason)
    }
}

