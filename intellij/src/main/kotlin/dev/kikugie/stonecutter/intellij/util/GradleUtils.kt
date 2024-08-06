package dev.kikugie.stonecutter.intellij.util

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings as ESTES
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.nio.file.Path

fun runGradleTask(project: Project, dir: Path, configuration: ESTES.() -> Unit) {
    val settings = ESTES().apply {
        externalSystemIdString = GradleConstants.SYSTEM_ID.id
        externalProjectPath = dir.toString().replace('\\', '/')
        configuration()
    }

    ExternalSystemUtil.runTask(
        settings,
        DefaultRunExecutor.EXECUTOR_ID,
        project,
        GradleConstants.SYSTEM_ID,
        null,
        ProgressExecutionMode.IN_BACKGROUND_ASYNC,
        false
    )
}