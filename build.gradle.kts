import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.AbstractDokkaParentTask
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import java.net.URI
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}

group = property("group").toString()
version = property("version").toString()

val unzipTarget = rootProject.layout.buildDirectory.file("dokka/versions").get().asFile
val String.URL get() = URI.create(this).toURL()

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.dokka.base)
        classpath(libs.dokka.versioning)
        classpath(libs.zip4j)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    dokkaPlugin(libs.dokka.versioning)
}

tasks.register("extractOldDocs") {
    group = "documentation"
    val source = projectDir.resolve("docs/kdoc")
    inputs.files(fileTree(source).matching { include("**/*.zip") })
    outputs.dir(unzipTarget)

    doLast {
        if (unzipTarget.exists()) unzipTarget.deleteRecursively()
        source.listFiles()!!.filter { it.extension == "zip" }.forEach {
            it.unzip(unzipTarget.resolve(it.nameWithoutExtension))
        }
    }
}

tasks.register("updateVersion") {
    val version4 = project.property("version4").toString()
    doLast {
        rename("docs/.vitepress/sidebars/versioned", "0\\.4\\.\\d.json", "$version4.json")
        rename("docs/versions", "0\\.4\\.\\d", version4)
        replace("build.gradle.kts", "/0\\.4\\.\\d", "/$version4")
        replace("docs/.vitepress/config.mts", "latestVersion: '.+'", "latestVersion: '$version'")
        replace("docs/stonecutter/guide/setup.md", "stonecutter\"\\ version \".+\"", "stonecutter\" version \"$version\"")
        replace("docs/stonecutter/guide/setup.md", "stonecutter\"\\) version \".+\"", "stonecutter\") version \"$version\"")
        replace(
            "stonecutter/src/main/kotlin/dev/kikugie/stonecutter/controller/StonecutterController.kt",
            "\"Running Stonecutter .+\"",
            "\"Running Stonecutter $version\""
        )
    }
}

tasks.register("updateHallOfFame") {
    doLast {
        val template = project.file("util/index.md")
        val mods = project.file("hall-of-fame.yml")

        val projects = ProjectFinder.find(mods)
        val text = template.readText().format(projects)
        val paths = mapOf(
            "" to project.file("docs/index.md"),
            "/0.4.5" to project.file("docs/versions/0.4.5/index.md")
        )
        for ((version, file) in paths) file.toPath().writeText(
            text.replace(": /stonecutter", ": $version/stonecutter"),
            Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}

tasks.withType<AbstractDokkaParentTask> {
    moduleName = "Stonecutter KDoc"

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        homepageLink = "https://stonecutter.kikugie.dev/"
        footerMessage = "(c) 2024 KikuGie"
    }

    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        version = project.version.toString()
        olderVersionsDir = unzipTarget
        renderVersionsNavigationOnAllPages = true
    }

    dependsOn(tasks.named("extractOldDocs"))
}

subprojects {
    tasks.withType<AbstractDokkaLeafTask> {
        dokkaSourceSets.configureEach {
            reportUndocumented = true
            skipEmptyPackages = true
            suppressObviousFunctions = true
            suppressInheritedMembers = true

            sourceLink {
                localDirectory.set(projectDir)
                remoteUrl.set("https://github.com/kikugie/stonecutter/tree/0.5/${project.name}/".URL)
                remoteLineSuffix.set("#L")
            }

            externalDocumentationLink {
                url = "https://docs.gradle.org/current/kotlin-dsl/".URL
                packageListUrl = "https://docs.gradle.org/current/kotlin-dsl/gradle/package-list".URL
            }

            externalDocumentationLink {
                url = "https://kotlinlang.org/api/core/".URL
            }

            externalDocumentationLink {
                url = "https://kotlinlang.org/api/kotlinx.serialization/".URL
            }
        }
    }
}