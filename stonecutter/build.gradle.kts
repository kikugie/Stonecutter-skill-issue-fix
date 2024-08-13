@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.mpp)
    alias(libs.plugins.shadow)
    alias(libs.plugins.gradle.publishing)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}

val stonecutter: String = "0.4" // by project

group = "dev.kikugie"
version = stonecutter

repositories {
    mavenCentral()
}

dependencies {
    api(project(":stitcher"))
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kaml)
    implementation(libs.cbor)
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Stonecutter Plugin")
    dokkaSourceSets {
        configureEach {
            reportUndocumented = true
            skipEmptyPackages = true
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_16)
    }
}

tasks.shadowJar {
    archiveBaseName.set("shadow")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

tasks.register("publishAll") {
    group = "publishing"
    dependsOn(tasks.publish, tasks.publishPlugins)
}

publishing {
    repositories {
        maven {
            name = "kikugieMaven"
            url = uri("https://maven.kikugie.dev/releases")
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

publishMods {
    version = stonecutter
    displayName = "Stonecutter [$stonecutter]"
    type = when {
        "alpha" in stonecutter -> ALPHA
        "beta" in stonecutter -> BETA
        else -> STABLE
    }
    changelog = """
        ## Installation
        ```kts
        pluginManagement {
            repositories {
                maven("https://maven.kikugie.dev/releases")
            }
        }

        plugins {
            id("dev.kikugie.stonecutter") version "$stonecutter"
        }
        ```
        
        ## Changelog
        ${rootProject.file("CHANGELOG.md").readText()}
    """.trimIndent()
    github {
        repository = "kikugie/stonecutter"
        accessToken = property("githubToken").toString()
        commitish = "0.4"
        tagName = "v$stonecutter"
        allowEmptyFiles = true
    }
}

gradlePlugin {
    website = "https://stonecutter.kikugie.dev/"
    vcsUrl = "https://github.com/kikugie/stonecutter"

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