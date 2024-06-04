plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "dev.kikugie"
version = "0.1.0-alpha.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stitcher"))
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform
}