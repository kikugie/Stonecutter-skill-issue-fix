@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "1.9.22"
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "dev.kikugie"
version = "0.3.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("script-runtime"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(16)
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