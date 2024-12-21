@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalPathApi::class)

import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readLines
import kotlin.io.path.walk
import kotlin.io.path.writeText

plugins {
    java
    `kotlin-dsl`
    alias(libs.plugins.shadow)
    alias(libs.plugins.gradle.publishing)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}

sourceSets {
    val samples by registering {
        kotlin.srcDir("src/samples/kotlin")
        compileClasspath += sourceSets.main.get().output
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":stitcher"))
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kaml)

    testImplementation(libs.bundles.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<AbstractDokkaLeafTask>().configureEach {
    moduleName.set("Stonecutter Gradle")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_16)
    }
    dependsOn(rootProject.tasks.named("updateVersion"))
}
java {
    withSourcesJar()
    withJavadocJar()
}

tasks.shadowJar {
    archiveBaseName.set("shadow")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

tasks.withType<AbstractDokkaLeafTask> {
    moduleName = "Stonecutter Gradle"
    dokkaSourceSets.configureEach {
        samples.from("src/samples/kotlin")
    }
}

tasks.register("addWikiLinks") {
    val properties: MutableMap<String, String> = mutableMapOf()
    fun wiki(id: String, page: String, title: String = "Wiki page") {
        properties["wiki-$id"] = "<a href=\"https://stonecutter.kikugie.dev/stonecutter/$page\">$title</a>"
    }

    fun Path.transform(): String? {
        var modified = false
        val lines = readLines()
        val new = buildList {
            var marker: String? = null

            for (it in lines) {
                var line: String = it
                when {
                    it.trimStart().startsWith("//") && "link:" in it -> marker =
                        it.substringAfter("link:").trim().takeIf { it in properties }
                    "*/" in it -> marker = null
                    marker != null && "@see" in it && "stonecutter" in it -> line =
                        it.replaceAfter("@see ", properties[marker]!!).also { modified = true }
                }
                add(line)
            }
        }
        return new.takeIf { modified }?.joinToString("\n")
    }

    wiki("eval", "guide/setup#checking-versions")
    wiki("chisel", "guide/setup#chiseled-tasks")
    wiki("settings", "guide/setup#settings-settings-gradle-kts")
    wiki("controller", "guide/setup#controller-stonecutter-gradle-kts")
    wiki("controller-params", "guide/setup#global-parameters")
    wiki("controller-active", "guide/setup#active-version")
    wiki("build", "guide/setup#versioning-build-gradle-kts")
    wiki("build-swaps", "guide/comments#value-swaps")
    wiki("build-consts", "guide/comments#condition-constants")
    wiki("build-deps", "guide/comments#condition-dependencies")

    doLast {
        project.file("src/main/kotlin/dev/kikugie/stonecutter").toPath().walk().forEach {
            val transformed = it.transform()
            if (transformed != null) it.writeText(transformed)
        }
    }
}

publishing {
    repositories {
        maven {
            name = "kikugieMaven"
            url = uri("https://maven.kikugie.dev/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create("basic", BasicAuthentication::class)
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = project.group.toString()
            artifactId = "stonecutter"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

gradlePlugin {
    website = "https://stonecutter.kikugie.dev/"
    vcsUrl = "https://github.com/stonecutter-versioning/stonecutter"

    plugins {
        create("stonecutter") {
            id = "dev.kikugie.stonecutter"
            implementationClass = "dev.kikugie.stonecutter.StonecutterPlugin"
            displayName = "Stonecutter"
            description = "Modern Gradle plugin for multi-version management"
            tags = setOf("minecraft", "mods")
        }
    }
}