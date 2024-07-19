@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.DokkaTask


plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.serialization)
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stitcher"))
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kaml)
    implementation(libs.cbor)
}

tasks.test {
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

kotlin {
    jvmToolchain(16)
}

java {
    withSourcesJar()
    withJavadocJar()
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
    website = "https://github.com/kikugie/stonecutter"
    vcsUrl = "https://github.com/kikugie/stonecutter"

    plugins {
        create("stonecutter") {
            id = "dev.kikugie.stonecutter"
            implementationClass = "dev.kikugie.stonecutter.StonecutterPlugin"
            displayName = "Stonecutter"
            description = "Preprocessor/JCP inspired multi-version environment manager"
        }
    }
}