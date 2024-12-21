package stonecutter_samples

import dev.kikugie.stonecutter.build.StonecutterBuild

private val stonecutter: StonecutterBuild = TODO("This is a sample, it must not be instantiated.")

fun allowExtensionsVararg() {
    stonecutter.allowExtensions("yml", "yaml")
}

fun allowExtensionsIterable() {
    val extensions = listOf("yml", "yaml")
    stonecutter.allowExtensions(extensions)
}

fun overrideExtensionsVararg() {
    stonecutter.overrideExtensions("scala", "sc")
}

fun overrideExtensionsIterable() {
    val extensions = listOf("scala", "sc")
    stonecutter.overrideExtensions(extensions)
}

fun excludeFilesVararg() {
    stonecutter.excludeFiles("src/main/resources/properties.json5")
}

fun excludeFilesIterable() {
    val files = listOf("src/main/resources/properties.json5")
    stonecutter.excludeFiles(files)
}