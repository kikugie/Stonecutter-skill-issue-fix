import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin

plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.20")
        classpath("org.jetbrains.dokka:versioning-plugin:1.9.20")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:1.9.20")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
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
    fun replaceAndWrite(path: String, pattern: String, replacement: String) {
        val location = project.file(path)
        require(location.exists() && location.isFile && location.canRead()) { "Path $path is invalid" }
        val text = location.readText()
        val new = text.replace(Regex(pattern, RegexOption.MULTILINE), replacement)
        if (new != text) location.writeText(new)
    }

    val version = project.property("stonecutter")
    replaceAndWrite("docs/.vitepress/config.mts", "latestVersion: '.+'", "latestVersion: '$version'")
    replaceAndWrite("docs/stonecutter/migration.md", "version \".+\"$", "version \"$version\"")
    replaceAndWrite(
        "stonecutter/src/main/kotlin/dev/kikugie/stonecutter/StonecutterController.kt",
        "\"Running Stonecutter .+\"",
        "\"Running Stonecutter $version\""
    )
}
