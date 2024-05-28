# Stonecutter configuration
This page covers Stonecutter methods available in `build.gradle[.kts]` or `stonecutter.configureEach { }` in `stonecutter.gradle[.kts]`.  
For more information, visit the Dokka page.

## Processor config
### Swaps
Swaps can be added like this:
::: code-group
```kotlin [build.gradle.kts]
stonecutter.swap("swap_token") {
    if (stonecutter.current.version > "1.20.1") "func1()" else "func2()"
}
```

```groovy [build.gradle]
stonecutter.swap("swap_token") {
    stonecutter.current.version > "1.20.1" ? "func1()" : "func2()"
}
```
:::
The first argument is the identifier string, used in the comment. The second is the replacement value.

### Constants
Constants add variables, that can be checked in the code. 
Constant names can contain spaces, but can't contain values from the syntax reference.
::: code-group
```kotlin [build.gradle[.kts]]
stonecutter.const("fabric", isFabric)
stonecutter.const("forge", isForge)
```
```kotlin [code.kt]
//? if fabric
println("good")
```
:::

### Expressions
Expressions are basically constants on steroids, as they are evaluated dynamically at runtime.  
Expressions have to return a boolean result if the passed value is valid, or otherwise `null`.  
Expressions must not be random or have side effects, since results are cached.
::: code-group
```kotlin [build.gradle[.kts]]
stonecutter.expression {
    if (it == "fabric") return isFabric
    else if (it == "forge") return isForge
    else return null
}
```
```kotlin [code.kt]
//? if fabric
println("good")
```
:::
As you can see, usually constants provide easier syntax, but if you need this functionality - it's there.

### Dependencies
Stonecutter dependencies allow specifying additional targets for the version checker.
::: code-group
```kotlin [build.gradle[.kts]]
stonecutter.dependency("sodium", "0.5.3")
```
```kotlin [code.kt]
//? if sodium: >0.6
/*withSodium6()*/
```
:::
Implicit version checks default to `minecraft`, which means you can write:
```kotlin [code.kt]
//? if minecraft: >1.20.1
func()
```
You can also overwrite the checked minecraft dependency this way:
```kotlin [build.gradle[.kts]]
val mcVersion = stonecutter.current.version // = 1.20.1
stonecutter.dependency("minecraft", "1.20.6")
```
This may be useful in cases where you have both multi-version and multi-loader setup, but be careful with this knowledge.

### Filters
Files can be filtered with the following functions:
```kotlin
stonecutter.whitelist { it -> // Path
}

stonecutter.blacklist { it -> // Path
}
```

## Plugin properties
### Project info
Object returned by the following methods contains:
- `project` - directory name of this subversion.
- `version` - specified Minecraft version.
- `isActive` - whenever this subversion is selected as active.

### Current version
Accessed with `stonecutter.current` and returns the project info for this instance of the buildscript.

### Active version
Accessed with `stonecutter.active` and returns the project info for the current active version, 
which is the same for all buildscript instances.

### All versions
Accessed with `stonecutter.versions` and returns the list of all registered subversions.

## Global config
Global options are configured in `stonecutter.gradle[.kts]`.  
Currently the only available option is `stonecutter.includeResources = true`, 
which removes the default `.java` and `.kt` file filter.