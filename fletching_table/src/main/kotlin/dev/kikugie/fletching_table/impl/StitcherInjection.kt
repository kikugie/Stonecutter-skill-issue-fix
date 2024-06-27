package dev.kikugie.fletching_table.impl

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.testFramework.LightVirtualFile
import dev.kikugie.fletching_table.lang.StitcherFile
import dev.kikugie.fletching_table.lang.StitcherLang

class StitcherInjector : LanguageInjector {
    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        registrar: InjectedLanguagePlaces,
    ) {
        if (host !is PsiComment) return
        val char = ElementManipulators.getValueText(host).firstOrNull()
        if (char != '?' && char != '$') return
        val range = ElementManipulators.getValueTextRange(host)
        registrar.addPlace(StitcherLang, range, null, null)
    }
}

@Suppress("UnstableApiUsage")
class StitcherFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? =
        if (file is LightVirtualFile && file.language == StitcherLang) StitcherFile.Type else null
}