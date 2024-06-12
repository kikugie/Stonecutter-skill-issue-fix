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

tasks.register("updateVersion") {
    fun replaceAndWrite(path: String, pattern: String, replacement: String) {
        val location = project.file(path)
        require(location.exists() && location.isFile && location.canRead()) { "Path $path is invalid" }
        val text = location.readText()
        val new = text.replace(Regex(pattern), replacement)
        if (new != text) location.writeText(new)
    }

    val version = project.property("stonecutter")
    replaceAndWrite("docs/.vitepress/config.mts", "latestVersion: '.+'", "latestVersion: '$version'")
    replaceAndWrite("docs/stonecutter/migration.md", "version \".+\"", "version \"$version\"")
    replaceAndWrite("stonecutter/src/main/kotlin/dev/kikugie/stonecutter/StonecutterController.kt", "\"Running Stonecutter .+\"", "\"Running Stonecutter $version\"")
}
