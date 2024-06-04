package dev.kikugie.fletching_table.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import javax.swing.Icon

class StitcherFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, StitcherLang) {
    override fun getFileType(): FileType = Type

    object Type : FileType {
        override fun getName(): String = "Stitcher"
        override fun getDescription(): String = "Stitcher comments"
        override fun getDefaultExtension(): String = "stitcher"
        override fun getIcon(): Icon? = null
        override fun isBinary(): Boolean = true
        override fun isReadOnly(): Boolean = true
    }
}