package dev.kikugie.fletching_table.lang

import com.intellij.lang.Language

object StitcherLang : Language("Stitcher") {
    private fun readResolve(): Any = StitcherLang
}