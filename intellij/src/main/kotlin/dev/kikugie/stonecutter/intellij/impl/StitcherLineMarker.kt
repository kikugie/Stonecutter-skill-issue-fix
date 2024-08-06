package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.ui.VersionSelectionList
import dev.kikugie.stonecutter.intellij.util.findMatching
import dev.kikugie.stonecutter.intellij.util.string
import java.awt.event.MouseEvent
import java.nio.file.Path
import javax.swing.Icon
import kotlin.jvm.optionals.getOrNull

class StitcherLineMarkerProvider : RelatedItemLineMarkerProvider(), GutterIconNavigationHandler<PsiElement> {
    override fun getName(): String = "Stonecutter line marker"
    override fun getIcon(): Icon = PluginAssets.SWITCH_TO_VERSION

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (element !is StitcherFile) return
        val module = element.module ?: return
        val service = element.stonecutterService
        val models = service.getProjectModels(module)
        val text = element.string

        val dir = service.getModulePath(module).getOrNull()?.doubleParent
            ?: return
        val matching = text.findMatching(models)
            .takeIf { it.isNotEmpty() }
            ?.map { it.project }
            ?.sorted()
            ?: return
        element.putUserData(Constants.PROJECTS, matching)
        element.putUserData(Constants.DIRECTORY, dir)

        val marker = NavigationGutterIconBuilder.create(icon)
            .setTargets(element)
            .setTooltipText("Switch to version")
            .createLineMarkerInfo(element, this)
        result.add(marker)
    }

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val matching = element.getUserData(Constants.PROJECTS) ?: return
        val directory = element.getUserData(Constants.DIRECTORY) ?: return
        JBPopupFactory
            .getInstance()
            .createListPopup(VersionSelectionList(directory, matching))
            .show(RelativePoint(event))    }

    private object Constants {
        val PROJECTS = Key.create<List<String>>("stonecutter.line-marker.matching-projects")
        val DIRECTORY = Key.create<Path>("stonecutter.line-marker.root-path")
    }
}