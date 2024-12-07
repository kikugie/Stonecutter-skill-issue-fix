plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0-RC")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.charleskorn.kaml:kaml:0.60.0")
    implementation("com.github.ajalt.mordant:mordant:2.7.2")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
}