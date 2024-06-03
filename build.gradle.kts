plugins {
    kotlin("jvm") version "1.9.22"  apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

tasks.dokkaHtmlCollector {
    moduleName = "Stonecutter KDoc"
}