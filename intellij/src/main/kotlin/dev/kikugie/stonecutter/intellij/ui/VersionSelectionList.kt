package dev.kikugie.stonecutter.intellij.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import dev.kikugie.stonecutter.intellij.util.runGradleTask
import java.nio.file.Path

class VersionSelectionList(private val project: Project, private val dir: Path, values: List<String>) : BaseListPopupStep<String>("Project", values) {
    override fun onChosen(selected: String, final: Boolean): PopupStep<*>? {
        if (final) switchTo(selected)
        return null
    }

    private fun switchTo(version: String) = runGradleTask(project, dir) {
        taskNames = listOf("\"Set active project to $version\"")
    }
}