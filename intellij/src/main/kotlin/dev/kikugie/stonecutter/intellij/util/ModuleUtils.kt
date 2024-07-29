package dev.kikugie.stonecutter.intellij.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.kikugie.stonecutter.intellij.impl.StonecutterService

val PsiElement.module get() = ModuleUtil.findModuleForPsiElement(this)
val PsiFile.module get() = ModuleUtil.findModuleForFile(this)

val PsiElement.stonecutterService get() = project.getServiceIfCreated(StonecutterService::class.java)
val Module.stonecutterService get() = project.getServiceIfCreated(StonecutterService::class.java)
val Module.stonecutterModel get() = stonecutterService?.get(this)