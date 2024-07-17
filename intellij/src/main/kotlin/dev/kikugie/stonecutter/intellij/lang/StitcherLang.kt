package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.Language

object StitcherLang : Language("Stitcher") {
    private fun readResolve(): Any = StitcherLang
}