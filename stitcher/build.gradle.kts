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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    testImplementation("com.charleskorn.kaml:kaml:0.57.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("com.github.ajalt.mordant:mordant:2.2.0")
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
            sourceRoots.setFrom(file("src/main/kotlin/dev/kikugie/stitcher"), file("src/main/kotlin/dev/kikugie/semver"))
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
            artifactId = "stitcher"
            version = project.version.toString()
            artifact(tasks.getByName("jar"))
        }
    }
}