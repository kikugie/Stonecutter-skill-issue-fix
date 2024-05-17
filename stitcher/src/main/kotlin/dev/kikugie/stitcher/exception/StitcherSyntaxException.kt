package dev.kikugie.stitcher.exception

import dev.kikugie.stitcher.data.Token

class StitcherSyntaxException(val token: Token, val reason: String) : RuntimeException(reason), StitcherThrowable