# Stonecutter tips & tricks

This section is not essential for working with Stonecutter but describes some common addons and practices.

## Versioned files

Although your versioned code is stored in the root project's `src` folder, 
directories under `versions/` are valid Gradle projects too.

This means you can put files in those folders, and they will be applied for each version separately. 
However, it is quite redundant since you need a copy for each version, which becomes harder to maintain.

The following sections will cover more efficient ways to achieve it, 
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
J52J converts those files to plain JSON after Stonecutter has done its work. 
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

This can be solved with versioned resource expansion:
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

## Architectury setup

> [!WARNING]
> Enabling Architectury along with Stonecutter requires significant project structure changes and decent Gradle knowledge.  
> The proper Architectury support is being worked on for Stonecutter 0.5.
> 
> (Basically,
> if you look at the [Elytra Trims build file](https://github.com/kikugie/elytra-trims/blob/kotlin/build.gradle.kts) 
> and think "yea, I get it", feel free to try).

Since there's a great variety of setups, there's no plug-in template. 
This guide covers the most bare-bone setup for Fabric/Forge on 1.20.1 and Fabric/Neoforge on 1.20.6, 
but be ready to adjust it to your project's needs.

### Structure changes

In this setup the common Architectury structure of `:common`, `:fabric`, `:neoforge` projects doesn't exist. 
Instead, everything is in the `src/` folder.  
This begs the question "How do I make the code for different loaders then?" - With comments!
Welcome to Stonecutter, comments are the solution to every problem!*
```
// TODO: Make this wiki more serious
```

### Buildscript setup

#### Settings

Settings finally got a use for `vers()` function! (Almost as if it was made for this use case)

::: code-group
```kotlin [settings.gradle.kts]
extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        vers("1.20.1-fabric", "1.20.1")
        vers("1.20.1-forge", "1.20.1")
        vers("1.20.6-fabric", "1.20.6")
        vers("1.20.6-neoforge", "1.20.6")
    }
    create(rootProject)
}
```
```groovy [settings.gradle]
stonecutter {
    shared {
        vers("1.20.1-fabric", "1.20.1")
        vers("1.20.1-forge", "1.20.1")
        vers("1.20.6-fabric", "1.20.6")
        vers("1.20.6-neoforge", "1.20.6")
    }
    create rootProject
}
```
:::

#### Controller

To toggle code based on the loader, we need to define Stonecutter constants.
::: code-group
```kotlin [stonecutter.gradle.kts]
stonecutter configureEach {
    val platform = project.property("loom.platform").toString()
    stonecutter.const("fabric", platform == "fabric" )
    stonecutter.const("forge", platform == "forge" )
    stonecutter.const("neoforge", platform == "neoforge" )
}
```
```groovy [stonecutter.gradle]
stonecutter.configureEach {
    def platform = project.property("loom.platform").toString()
    stonecutter.const("fabric", platform == "fabric")
    stonecutter.const("forge", platform == "forge")
    stonecutter.const("neoforge", platform == "neoforge")
}
```
:::

#### Build file

The biggest mess is definitely here, since you need to manage multiple dependency sets.
::: code-group
```kotlin [build.gradle.kts]
val modId = property("mod.id").toString()
val modLoader = property("loom.platform").toString()
val mcVersion = stonecutter.current.version

dependencies {
    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    if (modLoader == "fabric") {
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    } else if (modLoader == "forge") {
        "forge"("net.minecraftforge:forge:${mcVersion}-${property("deps.fml")}")
    } else {
        "neoForge"("net.neoforged:neoforge:${property("deps.fml")}")
    }
}

loom {
    accessWidenerPath.set(rootProject.file("src/main/resources/$modId.accesswidener"))
    
    if (modLoader == "forge") forge {
        convertAccessWideners.set(true)
        mixinConfigs(
            "$modId.mixins.json",
        )
    }
}
```
```groovy [build.gradle]
def modId = property("mod.id").toString()
def modLoader = property("loom.platform").toString()
def mcVersion = stonecutter.current.version

dependencies {
    minecraft "com.mojang:minecraft:${mcVersion}"
    mappings "net.fabricmc:yarn:${property("deps.yarn")}:v2"
    if (modLoader == "fabric") {
        modImplementation "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}"
    } else if (modLoader == "forge") {
        forge "net.minecraftforge:forge:${mcVersion}-${property("deps.fml")}"
    } else {
        neoForge "net.neoforged:neoforge:${property("deps.fml")}"
    }
}

loom {
    accessWidenerPath.set(getRootProject().file("src/main/resources/${modId}.accesswidener"))
    
    if (modLoader == "forge") forge {
        convertAccessWideners.set(true)
        mixinConfigs(
            "${modId}.mixins.json",
        )
    }
}
```
:::

### Resources

Similarly, resources have to be put together as well:
``` 
- src/main/resources
  |- META-INF
  |  |- mods.toml
  |  \- neoforge.mods.toml
  |- modid.accesswidener
  |- modid.mixins.json
  |- fabric.mod.json
  \- pack.mcmeta
```

You can apply previous tips to modify these files to your needs.

### Code changes

It is recommended to separate your loader-dependent logic into helper classes, such as:
```java [ModAccess.java]
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;

public class ModAccess {
    public static boolean isLoaded(String mod) {
        return FabricLoader.getInstance().isModLoaded(mod);
    }
}
//?} elif forge {
/*import net.minecraftforge.fml.loading.FMLLoader;

public class ModAccess {
    public static boolean isLoaded(String mod) {
        return FMLLoader.getModList().getModFileById(mod) != null;
    }
}
*///?} else {
/*import net.neoforged.fml.loading.FMLLoader

public class ModAccess {
    public static boolean isLoaded(String mod) {
        return FMLLoader.getModList().getModFileById(mod) != null;
    }
}
*///?}
```
As long as the specified methods are the same, swapping the class definition won't cause any issues.