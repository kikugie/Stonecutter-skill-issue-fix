package dev.kikugie.stonecutter.controller.manager

import dev.kikugie.stonecutter.Identifier
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

internal object GroovyController : ControllerManager {
    override val filename = "stonecutter.gradle"
    override fun createHeader(file: Path, version: Identifier) {
        file.writeText(
            """
            plugins.apply "dev.kikugie.stonecutter"
            stonecutter.active "$version"
            
            stonecutter.registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
                setGroup "project"
                ofTask "build"
            }
            """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}