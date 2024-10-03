# Setting up Stonecutter
## Project settings
The standard stonecutter setup in `settings.gradle[.kts]` looks like this:
::: code-group
```kotlin [settings.gradle.kts]
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5-alpha.8"
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
    }
}

plugins {
    id "dev.kikugie.stonecutter" version "0.5-alpha.8"
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

For more configuration options, visit the [Project Settings](/stonecutter/settings) page.

## Project controller
Reload Gradle, and the following file will appear (depending on your configuration in `settings.gradle[.kts]`):
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
You may notice the `/* [SC] DO NOT EDIT */` sign and I repeat:
> [!IMPORTANT]
> **Do not edit that line at all.**

This configuration file is global for all versions.
When you want to build all the versions - use the generated `chiseledBuild` task.
Chiseled tasks are executed for all versions in parallel. 
See the [Project Controller](/stonecutter/controller) page for more information.

## Versioned properties
> [!NOTE]
> These examples are for Fabric mods and may be outdated.
> Please adjust them to your needs.

::: code-group
```properties [gradle.properties]
# See latest at https://fabricmc.net/develop/
mod.id=template
mod.version=1.0.0

deps.fabric_loader=0.15.11

deps.minecraft=[VERSIONED]
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
Versioned properties override global ones, but it's still recommended to write them as a placeholder.

> [!NOTE]
> Unfortunately, if you're a `libs.gradle.toml` enjoyer, you have to abandon your fate.
> Version catalogues can't have per-project versions, which makes them unusable.

## Project build
> [!NOTE]
> These examples are for Fabric mods and may be outdated.
> Please adjust them to your needs.

Lastly, you need to adjust your buildscript to use versioned properties:
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

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}
```
Alternatively, assigning variables with `val fabricApi: String by project` is allowed.

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

loom {
    runConfigs.all {
        ideConfigGenerated true // Run configurations are not created for subprojects by default
        runDir "../../run" // Use a shared run folder and create separate worlds
    }
}
```
You can also you `project.property` accessors if names don't contain any punctuation.
:::

Depending on your mod, you may need to adjust the buildscript more. 
For the configuration options visit the [Project Build](/stonecutter/build) page.

## Running & building the project
In Stonecutter there's only one active version at a time. 
Trying to compile other versions without running a chiseled task will result in an error.

To switch the active version, open the Gradle menu and in the `stonecutter` group you'll have the following tasks:
- "Refresh active version" - reruns the comment processor on the current version.
- "Reset active version" - switches the active version to `vcsVersion` from `settings.gradle[.kts]`. Remember to run this before commits.
- "Set active version to {}" - switches the active version to the selected one.

If you're using CLI - task names are exactly as said above, so you'll be able to use `./gradlew "Reset active version"`.

If you're forgetful, you can create a task that runs the currently active version:
```kts [build.gradle[.kts]]
if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(tasks.named("build"))
    }
}
```

To build all versions, run `chiseledBuild` mentioned above.