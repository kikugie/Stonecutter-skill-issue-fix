package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.token.Token

class StitcherSyntaxWarning(val token: Token, val reason: String) : RuntimeException(reason), StitcherThrowable