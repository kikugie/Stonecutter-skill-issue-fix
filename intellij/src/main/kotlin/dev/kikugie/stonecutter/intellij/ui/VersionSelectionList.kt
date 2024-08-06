package dev.kikugie.stonecutter.intellij.ui

import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import org.gradle.tooling.GradleConnector
import java.nio.file.Path

class VersionSelectionList(private val dir: Path, values: List<String>) : BaseListPopupStep<String>("Project", values) {

    override fun onChosen(selected: String, final: Boolean): PopupStep<*>? {
        if (final) switchTo(selected, dir)
        return null
    }

    companion object {
        fun switchTo(version: String, dir: Path) {
            val connector = GradleConnector.newConnector()
            connector.forProjectDirectory(dir.toFile())
            connector.connect().use {
                it.newBuild().forTasks("Set active project to $version").run()
            }
        }
    }
}