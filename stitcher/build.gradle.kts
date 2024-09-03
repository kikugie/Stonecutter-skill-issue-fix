import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.kotlin.serialization)

    testImplementation(libs.kaml)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
    testImplementation("com.github.ajalt.mordant:mordant:2.7.2")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Stitcher")
    dokkaSourceSets {
        configureEach {
            skipEmptyPackages = true
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_16)
    }
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