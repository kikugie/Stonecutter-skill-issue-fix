plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "dev.kikugie"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.charleskorn.kaml:kaml:0.57.0")
//    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")

    testImplementation(kotlin("test"))
    testImplementation("com.github.ajalt.mordant:mordant:2.2.0")
}