package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.Predicates
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.ImportHelper
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.eval.isEmpty
import dev.kikugie.stonecutter.intellij.util.parse
import dev.kikugie.stonecutter.intellij.util.string

/**
 * A rather cursed import optimiser to account for Stitcher comments.
 *
 * Uses the following rules:
 * - Non-versioned comments are moved to the top and optimized by the default settings.
 * - Versioned comments are moved to the bottom and not optimized.
 */
class StitcherImportManager : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is PsiJavaFile

    override fun processFile(file: PsiFile): Runnable {
        val imports: PsiImportList = (file as? PsiJavaFile)?.importList
            ?: return EmptyRunnable.getInstance()

        return Runnable {
            val project = file.getProject()
            val manager = PsiDocumentManager.getInstance(project)
            val document = manager.getDocument(file)
            if (document != null) manager.commitDocument(document)

            val nonImports = mutableListOf<PsiComment>()
            val allImports = mutableListOf<PsiImportStatementBase>()
            val regularImports = mutableListOf<PsiImportStatementBase>()
            val stitcherImports = mutableListOf<StitcherImports>()

            // Collect imports into groups
            for (it in imports.children) when (it) {
                is PsiComment -> {
                    val definition = it.string.parse()
                    val closed = stitcherImports.lastOrNull()?.closed != false // is null or true

                    if (definition == null) {
                        if (closed) nonImports += it
                        else stitcherImports.last().entries.last().comments += it
                    } else {
                        val entry = if (closed) StitcherImports().also { stitcherImports += it }
                        else stitcherImports.last()
                        entry.apply {
                            entries += StitcherEntry(it, definition)
                        }
                    }
                }
                is PsiImportStatementBase -> {
                    allImports += it
                    if (stitcherImports.lastOrNull()?.closed != false) regularImports += it
                    else stitcherImports.last().entries.last().list += it
                }
            }

            // Sort and stringify imports
            val helper = ImportHelper(JavaCodeStyleSettings.getInstance(file))
            var newImports = createDummyFile(file, allImports.joinToString("\n") { it.string }).importList!!
            val copyImports = createDummyFile(file, file.string).importList!!
            val regularEntries = regularImports.map { it.text }.toSet()
            copyImports.replace(newImports)
            newImports = helper.prepareOptimizeImportsResult(file, Predicates.alwaysTrue()) ?: copyImports
            newImports.children.forEach {
                if (it.text !in regularEntries) it.delete()
            }

            val joinedRegular = newImports.children.join()
            val joinedStitcher = stitcherImports.joinToString("\n\n") { jt ->
                jt.entries.joinToString("\n") {
                    buildList {
                        add(it.element)
                        addAll(it.comments)
                        addAll(it.list)
                    }.join()
                }
            }

            // Epic finale
            val text = buildList {
                addNotBlank(nonImports.join())
                addNotBlank(joinedRegular)
                addNotBlank(joinedStitcher)
            }.joinToString("\n\n") + "\n import java.lang.*;"
            newImports = createDummyFile(file, text).importList!!
            newImports.children.lastOrNull()?.delete()
            newImports.children.lastOrNull()?.delete()
            imports.replace(newImports)
        }
    }
}

private fun <T : PsiElement> Iterable<T>.join() = joinToString("\n") {it.text}
private fun <T : PsiElement> Array<T>.join() = joinToString("\n") {it.text}

private fun MutableList<String>.addNotBlank(str: String) {
    if (str.isNotBlank()) add(str)
}

private fun createDummyFile(source: PsiFile, text: String): PsiJavaFile {
    val factory = PsiFileFactory.getInstance(source.project)
    return factory.createFileFromText("_Dummy_.${source.fileType.defaultExtension}", source.language, text, false, false) as PsiJavaFile
}

private data class StitcherEntry(
    val element: PsiComment,
    val definition: Definition,
    val list: MutableList<PsiImportStatementBase> = mutableListOf(),
    val comments: MutableList<PsiComment> = mutableListOf(),
)

private class StitcherImports {
    val entries: MutableList<StitcherEntry> = mutableListOf()

    val closed
        get() = entries.lastOrNull().let {
            if (it?.definition?.extension != true) false
            else {
                if (it.definition.isEmpty()) true
                else it.list.isNotEmpty()
            }
        }
}