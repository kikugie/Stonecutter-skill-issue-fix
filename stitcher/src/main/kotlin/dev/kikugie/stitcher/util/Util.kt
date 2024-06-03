package dev.kikugie.stitcher.util

internal fun <T : CharSequence> T.leadingSpaces(): Int {
    var spaces = 0
    for (char in this)
        if (char.isWhitespace()) spaces++ else break
    return spaces
}

internal fun <T : CharSequence> T.trailingSpaces(): Int {
    var spaces = 0
    for (char in this.reversed())
        if (char.isWhitespace()) spaces++ else break
    return spaces
}
