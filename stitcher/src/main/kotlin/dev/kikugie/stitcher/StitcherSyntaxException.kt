package dev.kikugie.stitcher

import dev.kikugie.stitcher.token.Token

class StitcherSyntaxException(val token: Token, val reason: String) : RuntimeException(reason) {
}