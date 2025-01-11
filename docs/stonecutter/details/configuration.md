# Configuring Stonecutter

## Global parameters
Global parameters are available in `stonecutter.gradle[.kts]` and affect the overall behaviour of the plugin.

### `processFiles`
When set to `false` disables file processing and `chiseledSrc` source altogether.
This can be used if versioning is only used to verify compatibility with different dependencies
or a different code preprocessor is used.

### `defaultReceiver`
Versions specified in `settings.gradle[.kts]` are assigned to this property.
In comments this makes `//? if <1.21` and `//? if minecraft: <1.21` equivalent.  
The property allows changing `minecraft` to a different dependency if needed.

### `debug`
Saves ASTs used by Stonecutter in YAML format in `build/stonecutter-cache/debug`
and logs processing steps in the info logger.

## Versioned configuration
These are parameters available in `build.gradle[.kts]` or inside the `parameters { }` block in `stonecutter.gradle[.kts]`.
The majority has been covered in the guide section, so this section will focus on the
available variants rather than the use cases.

*The examples are provided only in Kotlin, but the Groovy implementation 
is mostly the same with minor syntactic differences.*

### Common functions
Constants, dependencies and swaps have several forms of assigning the values.

::: details Single value assignment
```kotlin
stonecutter {
    const("myconst", true)
    dependency("mydep", "1.0")
    swap("myswap", if (eval(mc, "<1.21")) "content1" else "content2")
}
```
:::
::: details Lambda assignment
```kotlin
stonecutter {
    const("myconst") {
        true
    }
    dependency("mydep") {
        "1.0"
    }
    swap("myswap") {
        if (eval(mc, "<1.21")) "content1" else "content2"
    }
}
```
:::
::: details Variadic argument assignment (Kotlin only)
```kotlin
stonecutter {
    consts("myconst" to true, "myconst2" to false)
    dependencies("mydep" to "1.0", "mydep2" to "2.0")
    swap("myswap" to "content1", "myswap2" to "content2")
}
```
:::
::: details List assignment (Kotlin only)
```kotlin
val constList = listOf("myconst" to true, "myconst2" to false)
val depsList = listOf("mydep" to "1.0", "mydep2" to "2.0")
val swapList = listOf("myswap" to "content1", "myswap2" to "content2")
stonecutter {
    consts(constList)
    dependencies(depsList)
    swap(swapList)
}
```
:::
::: details Map assignment (Kotlin only)
```kotlin
stonecutter {
    consts["myconst"] = true
    dependencies["mydep"] = "1.0"
    swaps["myswap"] = when {
        eval(mc, "<1.20") -> "content1"
        eval(mc, "<1.21") -> "content2"
        else -> "content3"
    }
}
```
:::

### Constant selection
`consts` function can instead be provided a value with a list of options:
```kotlin
val current = "option2"
stonecutter {
    consts(current, "option1", "option2", "option3")
}
```
Options can be provided as a variadic argument or an iterable.
All values will be assigned a constant, but only the one matching selector will be `true`.

::: details Assigning mod loader constants
Given a setup with both multiple Minecraft versions and mod loaders, such as:
```text
- versions
  |- 1.20.1-fabric
  |- 1.20.1-forge
  |- 1.21.1-fabric
  \- 1.21.1-neoforge
```
This function can be used to assign multiple constants in one call:
```kotlin
stonecutter.parameters {
    val loader = metadata.project.substringAfterLast()
    consts(loader, "fabric", "forge", "neoforge")
}
```
:::

### File filtering
::: warning Deprecations notice
This functionality will be removed entirely in Stonecutter 0.6, 
resulting in build-time errors if you use this.  
When updating to 0.6 refer to the wiki for the up-to-date solution.
:::

Stonecutter allows excluding certain files or file formats from processing.

Files can only be excluded from the shared `src` directory with the given path string.
```kotlin
stonecutter.exclude("src/main/java/package/MyClass.java")
```

Specified file formats can be excluded altogether by starting the string with `*.` and the file format.
```kotlin
stonecutter.exclude("*.json")
```
