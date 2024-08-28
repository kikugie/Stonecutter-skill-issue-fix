import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import java.nio.file.StandardOpenOption
import kotlin.io.path.readLines
import kotlin.io.path.writeText

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.dokka.base)
        classpath(libs.dokka.versioning)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    dokkaHtmlPlugin(libs.dokka.versioning)
}

tasks.dokkaHtmlCollector {
    moduleName = "Stonecutter KDoc"
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        homepageLink = "https://stonecutter.kikugie.dev/"
        footerMessage = "(c) 2024 KikuGie"
    }
    val stonecutter = rootProject.property("stonecutter").toString()
    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        version = stonecutter
    }
}

tasks.register("updateVersion") {
    doLast {
        fun replaceAndWrite(path: String, pattern: String, replacement: String) {
            val location = project.file(path)
            require(location.exists() && location.isFile && location.canRead()) { "Path $path is invalid" }
            val text = location.readText()
            val new = text.replace(Regex(pattern, RegexOption.MULTILINE), replacement)
            if (new != text) location.writeText(new)
        }

        val version = project.property("stonecutter")
        replaceAndWrite("docs/.vitepress/config.mts", "latestVersion: '.+'", "latestVersion: '$version'")
        replaceAndWrite("docs/stonecutter/setup.md", "version \".+\"$", "version \"$version\"")
        replaceAndWrite(
            "stonecutter/src/main/kotlin/dev/kikugie/stonecutter/StonecutterController.kt",
            "\"Running Stonecutter .+\"",
            "\"Running Stonecutter $version\""
        )
    }
}

tasks.register("updateHallOfFame") {
    doLast {
        val template = project.file("util/index.md")
        val dest = project.file("docs/index.md")
        val mods = project.file("hall-of-fame.yml")

        val projects = ProjectFinder.find(mods)
        val text = template.readText().format(projects)
        dest.toPath().writeText(text, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}