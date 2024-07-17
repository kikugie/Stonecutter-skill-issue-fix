package dev.kikugie.stonecutter.intellij.util

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement

val PsiElement.isStitcherComment: Boolean get() {
    if (this !is PsiComment) return false
    val char = string.firstOrNull()
    return char == '?' || char == '$'
}

val PsiElement.string get() = ElementManipulators.getValueText(this)