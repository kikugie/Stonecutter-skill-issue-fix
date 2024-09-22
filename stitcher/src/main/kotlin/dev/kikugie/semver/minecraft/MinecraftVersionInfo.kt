package dev.kikugie.semver.minecraft

internal data class MinecraftVersionInfo(
    val nextRelease: String,
    val versions: List<String>
)