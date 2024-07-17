import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    java
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val stitcher: String by project

group = "dev.kikugie"
version = stitcher

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.kotlin.serialization)
    testImplementation(libs.kaml)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Stitcher")
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
            artifactId = "stitcher"
            version = project.version.toString()
            artifact(tasks.getByName("jar"))
        }
    }
}