@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    `kotlin-dsl`
    alias(libs.plugins.shadow)
    alias(libs.plugins.gradle.publishing)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":stitcher"))
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kaml)
    implementation(libs.cbor)
    implementation(libs.lz4)

    testImplementation(libs.bundles.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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