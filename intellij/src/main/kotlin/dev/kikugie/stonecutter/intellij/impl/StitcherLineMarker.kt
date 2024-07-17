package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.util.findMatching
import dev.kikugie.stonecutter.intellij.util.module
import dev.kikugie.stonecutter.intellij.util.string
import java.awt.event.MouseEvent
import javax.swing.Icon

class StitcherLineMarkerProvider : RelatedItemLineMarkerProvider(), GutterIconNavigationHandler<PsiElement> {
    override fun getName(): String = "Stonecutter line marker"
    override fun getIcon(): Icon = PluginAssets.SWITCH_TO_VERSION

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (element !is StitcherFile) return
        val module = element.module ?: return
        val text = element.string
        val matching = text.findMatching(module)
            .map { it.current.project }
            .takeIf { it.isNotEmpty() }
            ?: return
        element.putUserData(Constants.KEY, matching)

        val marker = NavigationGutterIconBuilder.create(icon)
            .setTargets(element)
            .setTooltipText("Switch to version")
            .createLineMarkerInfo(element, this)
        result.add(marker)
    }

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val matching = element.getUserData(Constants.KEY) ?: return

    }

    private object Constants {
        val KEY = Key.create<List<String>>("stonecutter.matching-projects")
    }
}