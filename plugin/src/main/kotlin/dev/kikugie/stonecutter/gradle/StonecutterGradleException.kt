package dev.kikugie.stonecutter.gradle

import org.gradle.api.GradleException

internal class StonecutterGradleException(message: String, suggestion: (() -> String)? = null) : GradleException(
    if (suggestion == null) "[Stonecutter] $message" else """
    [Stonecutter] $message
    Consider doing the following:
    $suggestion
""".trimIndent()
)