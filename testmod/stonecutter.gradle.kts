@file:OptIn(StonecutterDelicate::class)

import dev.kikugie.stonecutter.StonecutterDelicate
import dev.kikugie.stonecutter.RunConfigType

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
    //id("dev.kikugie.j52j") version "1.0.2" apply false // Enables asset processing by writing json5 files
    //id("me.modmuss50.mod-publish-plugin") version "0.7.+" apply false // Publishes builds to hosting websites
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

// Builds every version into `build/libs/{mod.version}/`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

/*
// Publishes every version
stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
*/

stonecutter parameters {
    /*
    See src/main/java/com/example/TemplateMod.java
    and https://stonecutter.kikugie.dev/
    */
    // Swaps replace the scope with a predefined value
    swap("mod_version", "\"${property("mod.version")}\";")
    // Constants add variables available in conditions
    const("release", property("mod.id") != "template")
    // Dependencies add targets to check versions against
    // Using `node.property()` in this block gets the versioned property
    val node = stonecutter.withProject(node!!)
    dependency("fapi", node.property("deps.fabric_api").toString())

    replacement(eval(metadata.version, ">=1.21").also { println("${metadata.project}:$it") },
        "Hello Fabric world!", "Hello new Fabric world!")
}
