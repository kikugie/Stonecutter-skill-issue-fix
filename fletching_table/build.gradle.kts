plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "dev.kikugie"
version = "0.1.0-alpha.1"

repositories {
    mavenCentral()
    maven("https://repo.gradle.org/gradle/libs-releases")
}

dependencies {
    implementation(project(":stitcher"))
    implementation(project(":stonecutter"))
    implementation("org.gradle:gradle-tooling-api:8.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.1")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.10")
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("org.jetbrains.plugins.gradle"))
}