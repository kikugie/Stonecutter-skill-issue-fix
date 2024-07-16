import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
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
        replaceAndWrite("docs/stonecutter/migration.md", "version \".+\"$", "version \"$version\"")
        replaceAndWrite(
            "stonecutter/src/main/kotlin/dev/kikugie/stonecutter/StonecutterController.kt",
            "\"Running Stonecutter .+\"",
            "\"Running Stonecutter $version\""
        )
    }
}

tasks.register("updateHallOfFame") {
    doLast {
        val dest = project.file("docs/index.md").toPath()
        val mods = project.file("hall-of-fame.txt").toPath()
        fun cleanLines(): List<String> {
            var yeet = false
            return dest.readLines().filterNot {
                if (it.trimStart().startsWith("let start")) {
                    yeet = true
                    return@filterNot false
                } else if (it.trimStart().startsWith("let end"))
                    yeet = false
                yeet
            }
        }
        val projects = ProjectFinder
            .find(*mods.readLines().toTypedArray())
            .sortedBy { -it.downloads }
            .joinToString(",\n") { it.toJS() }
        val text = cleanLines().joinToString("\n").replaceFirst(
            "let start = \"here\";",
            """let start = "here";
const members = [
$projects
];
""")
        dest.writeText(text)
    }
}