# Migrating to Stonecutter
You decided to use Stonecutter. My pleasure! Let me take you on a ride!

## Project settings
### Settings template
::: code-group
```kts [settings.gradle.kts]
import dev.kikugie.stonecutter.StonecutterSettings

pluginManagement {
    repositories {
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.4.1"
}

extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        versions("1.20.1", "1.20.4", "1.20.6")
        vers("1.21-snapshot", "1.21-alpha.24.20.a")
        vcsVersion = "1.20.6"
    }

    create(rootProject)
}
```

```groovy [settings.gradle]
pluginManagement {
    repositories {
        maven{
            url = "https://maven.kikugie.dev/releases"
        }
    }
}

plugins {
    id "dev.kikugie.stonecutter" version "0.4.1"
}

stonecutter {
    shared {
        versions "1.20.1", "1.20.6"
        vers("1.21-snapshot", "1.21-alpha.24.20.a")
        vcsVersion = "1.20.6"
    }

    create rootProject
}
```
:::

### Settings components
Stonecutter settings block is the first entrypoint for the project. It informs the rest of the plugin how to set up the environment.

::: tabs
== Kotlin DSL
In the Kotlin example these parameters are used if you want the rest of your project to use Kotlin DSL:  
```kts
kotlinController = true
centralScript = "build.gradle.kts"
```

== Shared block
`shared` block controls the version setup in a Stonecutter project:  
```kts
shared {
    versions("1.20.1", "1.20.6")
    vers("1.21-snapshot", "1.21-alpha.24.20.a")
    vcsVersion = "1.20.6"
}
```
It has 3 available functions:
- `versions` - simple setup, which creates project with the same directory name and minecraft version.
- `vers` - adds a project with separate directory and version.
- `vcsVersion` *(optional)* - sets which project is considered to be main. By default, it's the first registered project.

== Assigning project
Assigns the current configuration to a project. In the standard setup it's `rootProject`, but depending on the structure you can set it to any subproject you have.
```kts
create(rootProject)
```
:::

## Project controller
### Controller template
After setting up `settings.gradle[.kts]`, `stonecutter.gradle[.kts]` will be created with the following contents:
::: code-group
```kts [stonecutter.gradle.kts]
plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */
```

```groovy [stonecutter.gradle]
plugins {
    id "dev.kikugie.stonecutter"
}
stonecutter.active "1.20.6" /* [SC] DO NOT EDIT */
```
:::
You may notice the `/* [SC] DO NOT EDIT */` sign and I repeat:
> [!IMPORTANT]
> **Do not edit that line at all.**

### Global plugins
Apart from that the purpose of this file it to have a global configuration entrypoint.
For example you can add plugins like this:
```kts
plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.6-SNAPSHOT" apply false
}
```
This way it prevents Gradle from reconfiguring the plugin for every subproject.

### Chiseled tasks
Building the mod properly is a later topic, but as a sneak peek you need to use a chiseled task:
::: code-group
```kts [stonecutter.gradle.kts]
plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { // [!code focus:4]
    group = "project"
    ofTask("build")
}
```

```groovy [stonecutter.gradle]
plugins {
    id "dev.kikugie.stonecutter"
}
stonecutter.active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter.registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { // [!code focus:4]
    setGroup "project"
    ofTask "build"
}
```
:::
Chiseled tasks process the active version for all existing subprojects and then runs the specified task for them.

### Configure each
Configuring `build.gradle[.kts]` is a later topic as well, although not as far away, but for consistency you can use the following method:
```kts
// Same for Groovy
stonecutter.configureEach {
    
}
```
There you get an implicit receiver (AKA called with `this.`) of the `build.gradle[.kts]` plugin, where you can call all its methods.

## Versioned properties
Before we configure the build file, we need to set up versioned properties. For example the basic setup for a Fabric mod would look like this:
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
deps.minecraft=1.20.1
deps.yarn_mappings=1.20.1+build.10
deps.fabric_api=0.92.1+1.20.1
```
```properties [versions/1.20.6/gradle.properties]
# See latest at https://fabricmc.net/develop/
deps.minecraft=1.20.6
deps.yarn_mappings=1.20.6+build.1
deps.fabric_api=0.98.0+1.20.6
```
```properties [versions/1.21-snapshot/gradle.properties]
# See latest at https://fabricmc.net/develop/
deps.minecraft=24w20a
deps.yarn_mappings=24w20a+build.4
deps.fabric_api=0.98.2+1.21
```
:::
Versioned properties override global ones, but it's still recommended to write them as a placeholder.

> [!NOTE]
> Unfortunately, if you're a `libs.gradle.toml` enjoyer, you have to abandon your fate.
> Version catalogues can't have per-project versions, which makes them unusable.

## Build configuration
### Basic setup
This finally covers the contents of `build.gradle[.kts]` :tada:  

The very first change you'll need to make is to apply versioned dependencies:
::: tabs
== build.gradle.kts
```kts
dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and just create separate worlds
    }
}
```
Alternatively, assigning variables with `val fabricApi: String by project` is allowed.

== build.gradle
```groovy 
dependencies {
    minecraft "com.mojang:minecraft:${property('deps.minecraft')}"
    mappings "net.fabricmc:yarn:${property('deps.yarn_mappings')}:v2"
    modImplementation "net.fabricmc:fabric-loader:${property('deps.fabric_loader')}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${property('deps.fabric_api')}"
}

loom {
    runConfigs.all {
        ideConfigGenerated true // Run configurations are not created for subprojects by default
        runDir "../../run" // Use a shared run folder and just create separate worlds
    }
}
```
In case you wonder, **using `property("...")` is the only way**.
Due to Gradle being Gradle, using standard `project.fabric_api` etc. won't pull the correct dependency.
:::

### Notes
Depending on your project you may need to add versioned properties to other places, but the setup is now done.
Refer to other sections to learn how to test and manage versioned code.