package dev.kikugie.stitchertest.util

import com.github.ajalt.mordant.rendering.TextColors
import dev.kikugie.stitcher.data.token.Token

fun printCol(str: CharSequence) = str.split('\n').forEach { println(TextColors.cyan(it)) }

fun Sequence<Token>.printSetup() =
    forEach {
        printCol(
            "type(\"${
                it.value.replace("\n", "\\n").replace("\"", "\\\"")
            }\", ${it.type})"
        )
    }
