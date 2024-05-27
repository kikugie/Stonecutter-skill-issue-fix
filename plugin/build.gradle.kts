@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.DokkaTask


plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "dev.kikugie"
version = "0.4.0-alpha.7"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stitcher"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.1")
    implementation("com.charleskorn.kaml:kaml:0.57.0")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("script-runtime"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Stonecutter Plugin")
    dokkaSourceSets {
        configureEach {
            skipEmptyPackages = true
            sourceRoots.setFrom(file("src/main/kotlin/dev/kikugie/stonecutter/gradle"))
            includes.setFrom(fileTree("docs").files)
            samples.setFrom(fileTree("samples").files)
        }
    }
}

kotlin {
    jvmToolchain(16)
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
            artifact(tasks.getByName("jar"))
        }
    }
}

gradlePlugin {
    website = "https://github.com/kikugie/stonecutter-kt"
    vcsUrl = "https://github.com/kikugie/stonecutter-kt"

    plugins {
        create("stonecutter") {
            id = "dev.kikugie.stonecutter"
            implementationClass = "dev.kikugie.stonecutter.gradle.StonecutterPlugin"
            displayName = "Stonecutter"
            description = "Preprocessor/JCP inspired multi-version environment manager"
            tags.set(listOf("fabric", "fabricmc"))
        }
    }
}