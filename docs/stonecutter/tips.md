# Stonecutter tips & tricks

This section is not essential for working with Stonecutter, 
but describes some common addons and practices.

## Versioned files

Although your versioned code is stored in the root project's `src` folder, 
directories under `versions/` are valid Gradle projects too.

This means you can put files in those folders, and they will be applied for each version separately. 
However, it is quite redundant, since you need a copy for each version, which becomes harder to maintain.

The following sections will cover more efficient ways to accomplish it, 
but in case you need this method - now you know!

## J52J processing

When developing Minecraft mods, you often need to include `.json` files.  
However, the plain JSON format doesn't support comments, which makes Stonecutter unusable.
JSON5 is a good alternative, but it's not supported by Minecraft datapacks.

This can be solved with the [J52J](https://github.com/kikugie/j52j) plugin:
::: code-group
```kotlin [settings.gradle.kts]
pluginManagement {
    repositories {
        maven("https://maven.kikugie.dev/releases")
    }
}
```
```groovy [settings.gradle]
pluginManagement {
    repositories {
        maven { url = "https://maven.kikugie.dev/releases"}
    }
}
```
:::

::: code-group
```kotlin [build.gradle.kts]
plugins {
    id("dev.kikugie.j52j") version "1.0"
}
```
```groovy [build.gradle]
plugins {
    id "dev.kikugie.j52j" version "1.0"
}
```
:::

JSON5 files support both `//` and `/* */` comments, which makes processing them the same as the rest of the code. 
J52J converts those files to plain JSON after Stonecutter has done it's work. 
If needed, files can be kept as JSON5 with a `// no j52j` header.

## Blossom replacements

Stonecutter swaps are useful for replacing values like this:
```kotlin [build.gradle[.kts]] / [stonecutter.gradle[.kts]]
stonecutter {
    swap("mod_version", "\"${property("mod.version")}\";")
}
```
```java [ModConstants.java]
public class ModConstants {
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
}
```

However, what if the value you need to replace is inside a string?
```java [ModConstants.java]
public class ModConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.class)
    static {
        LOGGER.info("Running mod version 0.1.0")
    }
}
```

In this case, you can use [Blossom](https://github.com/KyoriPowered/blossom) plugin to replace it like this:
```kotlin [build.gradle[.kts]]
sourceSets {
    main {
        blossom {
            javaSources {
                property("version", property("mod.version").toString())
            }
        }
    }
}
```
```java [ModConstants.java]
public class ModConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.class)
    static {
        LOGGER.info("Running mod version {{ version }}")
    }
}
```

In this example the property `mod.version` is global, but it works on versioned values as well.

## Resource expansion

By default, Gradle includes a token replacement functionality of its own.  
And if you're using the [template mod](https://github.com/kikugie/stonecutter-template-fabric) you have it like this:
```kotlin [build.gradle.kts]
class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

val mod = ModData()
val mcVersion = stonecutter.current.version

tasks.processResources { // [!code focus:14]
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcVersion)

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcVersion
    )

    filesMatching("fabric.mod.json") { expand(map) }
}
```

However, this can be expanded further. For example, you may need a versioned access widener for your mod. 
For example, if you develop for `1.19.4`, `1.20.1`, `1.20.4`, `1.20.6` and `1.21`. 
If you need to change the file only when going from `1.19` to `1.20` and from `1.20.6` to `1.21`, 
making a versioned access widener will be very redundant.

This can solved with versioned resource expansion:
::: code-group
```kotlin [build.gradle.kts]
def mcVersion = stonecutter.current.version
def awVersion = if (stonecutter.compare(mcVersion, "1.21") >= 0) "1.21"
    else if (stonecutter.compare(mcVersion, "1.20") >= 0) "1.20"
    else "1.19"

loom {
    accessWidenerPath = rootProject.file("src/main/resources/aw/$awVersion.accesswidener")
}

tasks.processResources {
    // Add other variables too
    inputs.property("aw", awVersion)
    
    val map = mapOf("aw" to awVersion)
    filesMatching("fabric.mod.json") { expand(map) }
}
```
```groovy [build.gradle]
val mcVersion = stonecutter.current.version
val awVersion = stonecutter.compare(mcVersion, "1.21") >= 0 ? "1.21" :
        stonecutter.compare(mcVersion, "1.20") >= 0 ? "1.20" : "1.19"
loom {
    accessWidenerPath = getRootProject().file("src/main/resources/aw/${awVersion}.accesswidener")
}

processResources {
    // Add other variables too
    inputs.property("aw", awVersion)

    def map = ["aw": awVersion]
    filesMatching("fabric.mod.json") { expand(map) }
}
```
:::

You then put your access wideners like this:
```
- src/main/resources/aw
  |- 1.19.accesswidener
  |- 1.20.accesswidener
  \- 1.21.accesswidener
```

And add the correct path to the `fabric.mod.json`:
```json [fabric.mod.json]
  "accessWidener": "aw/${aw}.accesswidener",
```