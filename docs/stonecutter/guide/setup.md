# Setting up Stonecutter

This guide goes through the steps of setting up Stonecutter for a Fabric mod.
If you're starting a new project, using the [Stonecutter Fabric Template](https://github.com/stonecutter-versioning/stonecutter-template-fabric)
may be easier, but reading this is still recommended to understand what you're doing.
The guide assumes you start with an empty mod, like the [Fabric Template Mod](https://github.com/FabricMC/fabric-example-mod),
adding Stonecutter functionality on top.

If you're using a different platform, the principles remain the same, but you may need to adjust your code accordingly.  
*(Author's note: I can't write a guide for every kind of mod or plugin, thank you for understanding.)*

## Settings (`settings.gradle[.kts]`)
The settings setup has little variety, so it will be easier to show a full example and break it down next.
::: code-group
```kotlin [settings.gradle.kts]
pluginManagement {
    repositories {
        gradlePluginPortal()
        // For -alpha or -beta builds use this:
        // maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5-beta.5"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.20.1", "1.20.4", "1.20.6")
        vers("1.21-snapshot", "1.21-alpha.24.20.a")
        vcsVersion = "1.20.6"
    }
}
```

```groovy [settings.gradle]
pluginManagement {
    repositories {
        gradlePluginPortal()
        // For -alpha or -beta builds use this:
        // maven { url = "https://maven.kikugie.dev/snapshots" }
    }
}

plugins {
    id "dev.kikugie.stonecutter" version "0.5-beta.5"
}

stonecutter {
    create(rootProject) {
        versions "1.20.1", "1.20.6"
        vers("24w20a", "1.21-alpha.24.20.a")
        vcsVersion = "1.20.6"
    }
}
```
:::

Breaking up the components:
1. `kotlinController = true` enables `stonecutter.gradle.kts`, which will be used later.
2. `centralScript = "build.gradle.kts"` sets the versioned buildscript name.
3. `create()` selects a project to be multi-versioned and configures it in the scope.
4. `versions()` adds multiple simple versions.
5. `vers()` adds a version with directory different from the used semantic version.
6. `vcsVersion` specifies the version this multi-versioned project defaults to. Default is the first registered version.

::: details Stonecutter versions
In Stonecutter each "version" (also referred as "subproject") consists of two values: 
- Project name, used as a directory in `versions/`. This must be unique.
- Project version, used for conditional checks in buildscripts and source code. It can be a duplicate but must be either a semantic version or a string without whitespaces.

Using `versions()` creates subprojects, where these parameters are the same, whereas `vers()` allows them to be set separately.
:::

::: details VCS version
While using Stonecutter, you'll be switching between the versions, which modifies the source code you have.  
If you're using a version control system, such as Git (via GitHub, GitLab, etc.), these switches will clutter your commits,
which can cause merging issues.  

To mitigate this, Stonecutter assigns a "VCS version", which comes with the corresponding "Reset active project" Gradle task.
It is recommended to run the task before committing your changes.
:::

If you want to include versioned subprojects, see {TODO}

After setting up the versions you need, reload Gradle and move on to the next chapter.

## Controller (`stonecutter.gradle[.kts]`)
After the Gradle reload, this file will appear with default contents:
::: code-group
```kts [stonecutter.gradle.kts]
plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}
```

```groovy [stonecutter.gradle]
plugins {
    id "dev.kikugie.stonecutter"
}
stonecutter.active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter.registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    setGroup "project"
    ofTask "build"
}
```
:::

This file functions like a usual Gradle buildscript except that it runs once for all versions registered in `settings.gradle[.kts]`.

There are two common Stonecutter functions in this file:

### Chiseled tasks
Chiseled tasks are executed for all versioned subprojects. 
The generated task is used to create `.jar` for each Minecraft version you specified.

::: warning Running root tasks
Chiseled tasks **must** be used when building the mod for multiple versions.
Running the standard `build` task will result in an error or corrupt files.
:::

Chiseled tasks have various uses:
::: details Publishing the mod
You can publish all builds of your mod to Modrinth, Curseforge and GitHub using the [Mod Publish Plugin](https://modmuss50.github.io/mod-publish-plugin/).

Configure it as usual in `build.gradle[.kts]` and create a chiseled task for publishing all the builds:
::: code-group
```kts [stonecutter.gradle.kts]
stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
```

```groovy [stonecutter.gradle]
stonecutter.registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    setGroup "project"
    ofTask "publishMods"
}
```
:::

::: details Version selection
Chiseled tasks have a `versions` parameter to specify which subprojects should be processed.
For example, if versions `1.18.2`, `1.19.4`, `1.20.1` and `1.21` are registered, they can be filtered like this:
::: code-group
```kts [stonecutter.gradle.kts]
stonecutter registerChiseled tasks.register("buildAbove1.20", stonecutter.chiseled) {
    versions { _, it -> stonecutter.eval(it.version, ">=1.20") }
    group = "project"
    ofTask("build")
}
```

```groovy [stonecutter.gradle]
stonecutter.registerChiseled tasks.register("buildAbove1.20", stonecutter.chiseled) {
    versions { br, it -> stonecutter.eval(it.version, ">=1.20")}
    setGroup "project"
    ofTask "build"
}
```
:::

::: details Building any version
This allows building any version, even inactive ones, by creating individual chiseled tasks for each.
Note that this is less efficient than switching the active version or using chiseled builds for all subprojects
and may cause your IDE to incorrectly resolve library/Minecraft sources.

::: code-group
```kts [stonecutter.gradle.kts]
for (ver in stonecutter.versions) {
    stonecutter registerChiseled tasks.register("build-${ver.project}", stonecutter.chiseled) {
        versions { _, it -> it == ver }
        group = "project"
        ofTask("build")
    }
}
```

```groovy [stonecutter.gradle]
for (ver in stonecutter.versions) {
    stonecutter.registerChiseled tasks.register("build-${ver.project}", stonecutter.chiseled) {
        versions { br, it -> it == ver }
        setGroup("project")
        ofTask("build")
    }
}
```
:::

### Global parameters
Adding parameters is a topic of the next chapter and can be done in `build.gradle[.kts]` or in the parameters block:

::: code-group
```kts [stonecutter.gradle.kts]
stonecutter parameters {
    const("myconst", node!!.property("versioned_property"))
}
```

```groovy [stonecutter.gradle]
stonecutter.parameters {
    const("myconst", node.property("versioned_property"))
}
```
:::

## Properties (`gradle.properties`)
Before making the mod versioned, it needs to have the correct parameters for each subproject.
Gradle allows overriding `gradle.properties` entries for this purpose.

::: info NOTE
These examples are for Fabric mods and may be outdated.
Please adjust them for your needs.
:::

::: code-group
```properties [gradle.properties]
# See latest at https://fabricmc.net/develop/
mod.id=template
mod.version=1.0.0

deps.fabric_loader=0.15.11

deps.yarn_mappings=[VERSIONED]
deps.fabric_api=[VERSIONED]
```
```properties [versions/1.20.1/gradle.properties]
# See latest at https://fabricmc.net/develop/
deps.yarn_mappings=1.20.1+build.10
deps.fabric_api=0.92.1+1.20.1
```
```properties [versions/1.20.6/gradle.properties]
# See latest at https://fabricmc.net/develop/
deps.yarn_mappings=1.20.6+build.1
deps.fabric_api=0.98.0+1.20.6
```
```properties [versions/1.21-snapshot/gradle.properties]
# See latest at https://fabricmc.net/develop/
deps.yarn_mappings=24w20a+build.4
deps.fabric_api=0.98.2+1.21
```
:::

In this example, specifying versioned properties in the root file is not required,
but it may provide syntax completion in your IDE and make debugging easier.

### Version catalogues
You can't use `libs.gradle.toml` for versioned dependencies.
Version catalogues don't support per-project entries, which makes them unusable.

You can use them for plugins and Minecraft-independent libraries,
but this guide will use the standard Gradle properties for consistency.

## Versioning (`build.gradle[.kts]`)
Lastly, you need to adjust your buildscript to use versioned properties:

::: info NOTE
These examples are for Fabric mods and may be outdated.
Please adjust them for your needs.
:::

::: tabs
== build.gradle.kts
```kts
plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

tasks.processResources {
    inputs.property("minecraft", stonecutter.current.version)
    
    filesMatching("fabric.mod.json") { expand(mapOf(
        "minecraft" to stonecutter.current.version
    )) }
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}
```
Alternatively, you can assign properties to variables via the project delegate: `val fabricApi: String by project`.

== build.gradle
```groovy 
plugins {
    id "fabric-loom" version "1.7-SNAPSHOT"
}

dependencies {
    minecraft "com.mojang:minecraft:${stonecutter.current.project}"
    mappings "net.fabricmc:yarn:${property('deps.yarn_mappings')}:v2"
    modImplementation "net.fabricmc:fabric-loader:${property('deps.fabric_loader')}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${property('deps.fabric_api')}"
}

processResources {
    inputs.property "minecraft", stonecutter.current.version

    filesMatching("fabric.mod.json") {
        expand "minecraft": stonecutter.current.version
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated true // Run configurations are not created for subprojects by default
        runDir "../../run" // Use a shared run folder and create separate worlds
    }
}
```
You can also use `project.property` accessors if names don't contain any punctuation.
:::

## Checking versions
Since the `build.gradle[.kts]` script executes for every declared version,
you may want to adjust the setup based on the processed version.

This can be done using the `eval()` or `evalLenient()` functions.
`eval()` requires both arguments to be valid semantic versions, while the
lenient variant will check parameters lexicographically if any version
is not semantic. Both functions are available in `build.gradle[.kts]` and `stonecutter.gradle[.kts]`.
You can check the available predicate options in [Version predicates](./comments#version-predicates) 
and [Combined predicates](./comments#combined-predicates) sections.
::: code-group
```kotlin [build.gradle.kts]
java {
    withSourcesJar()
    val java = if (stonecutter.eval(minecraft, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}
```
```groovy [build.gradle]
java {
    withSourcesJar()
    def java = stonecutter.eval(minecraft, ">=1.20.5")
        ? JavaVersion.VERSION_21 : JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}
```
:::

## Developing your mod
After the setup is done, reload Gradle and the following tasks will appear:
- `project/chiseledBuild` - used to compile and package all versioned subprojects.
- `stonecutter/Refresh active project` - fixes any comment block states, without changing the active version.
- `stonecutter/Reset active version` - changes the active version to the `vcsVersion` parameter in `settings.gradle[.kts]`. Remember to run this before commits.
- `stonecutter/Set active version to {}` - switches the active version to the selected one.

::: info CLI usage
If you're running Gradle tasks from a terminal, you need to include the quotes: `./gradlew "Reset active version"`
:::

### Active version
The active version determines which Minecraft (and other libraries) version is applied to the source code.
Unless you're running a chiseled task, your source code **doesn't exist for other subprojects**.

Trying to build the mod or run the game for an inactive version may result in unpredictable errors, but the most common are:
- `java.lang.IllegalArgumentException: Cannot nest jars into none mod jar {mod name}`
- `java.io.UncheckedIOException: java.io.IOException: Invalid paths argument, contained no existing paths: [...]`
- `java.nio.file.NoSuchFileException: Could not find AW '{accesswidener file}' to convert into AT!`

*(Author's note: "WHY IS STONECUTTER NOT BUILDING I HATE GRADLE, oh my active version is wrong" is a surprisingly common occurrence.)*