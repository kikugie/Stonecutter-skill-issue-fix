package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.table.JBListTable
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.ui.VersionSelectionList
import dev.kikugie.stonecutter.intellij.util.findMatching
import dev.kikugie.stonecutter.intellij.util.module
import dev.kikugie.stonecutter.intellij.util.stonecutterService
import dev.kikugie.stonecutter.intellij.util.string
import java.awt.Color
import java.awt.event.MouseEvent
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JPanel

class StitcherLineMarkerProvider : RelatedItemLineMarkerProvider(), GutterIconNavigationHandler<PsiElement> {
    override fun getName(): String = "Stonecutter line marker"
    override fun getIcon(): Icon = PluginAssets.SWITCH_TO_VERSION

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (element !is StitcherFile) return
        val module = element.module ?: return
        val service = module.stonecutterService ?: return
        val text = element.string

        val matching = text.findMatching(module)
            .map { it.current.project }
            .takeIf { it.isNotEmpty() }
            ?: return
        element.putUserData(Constants.PROJECTS, matching)
        element.putUserData(Constants.DIRECTORY, service.roots[module])

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