# Project build

This section covers functionality available in `build.gradle[.kts]`.

## Property overview
### `stonecutter.tree`
> Allows traversing the project tree constructed in `settings.gradle[.kts]`.  
> See the [Project Tree](/stonecutter/project-tree) page for more information.

### `stonecutter.branch`
> Returns this version's branch, which allows iterating over all versions in it.  
> See the [Project Tree](/stonecutter/project-tree) page for more information.

### `stonecutter.node`
> Returns this version's project node, which can be used to access other nodes in this and other branches.  
> See the [Project Tree](/stonecutter/project-tree) page for more information.

### `stonecutter.versions`
> Returns all version ids in this version's branch. To get all globally registered ids, use `stonecutter.tree.versions`.

### `stonecutter.acvive`
> Returns the globally active version id. 
> May not be present in `stonecutter.versions` when using branches with different version sets.

### `stonecutter.current`
> Returns the version id of this buildscript instance.

## Processor config
### Constants
Constants add variables that can be checked in the code.
::: code-group
```kotlin [build.gradle[.kts]]
stonecutter.const("fabric", isFabric)
stonecutter.const("forge", isForge)
```
```java [code.java]
//? if fabric
System.out.println("good");
```
:::

Constants have multiple ways to be assigned:
```kotlin [build.gradle[.kts]]
stonecutter.const("name", true)
stonecutter.const("name") { true }

// Checks the provided option against the choices
stonecutter.consts(option, "option1", "option2")

// Kotlin DSL only!
stonecutter.consts["name"] = true
stonecutter.consts(
    "name" to true,
    "other_name" to false
)
```

### Dependencies
Stonecutter dependencies allow specifying additional targets for the version checker.
::: code-group
```kotlin [build.gradle[.kts]]
stonecutter.dependency("sodium", "0.5.3")
```
```java [code.java]
//? if sodium: >0.6
/*withSodium6();*/
```
:::

Implicit version checks default to `minecraft`, which means you can write:
```java [code.java]
//? if minecraft: >1.20.1
func();
```
You can also overwrite the checked minecraft dependency this way:
```kotlin [build.gradle[.kts]]
val mcVersion = stonecutter.current.version // = 1.20.1
stonecutter.dependency("minecraft", "1.20.6")
```
This may be useful in cases where you have a both multi-version and multi-loader setup, but be careful with this knowledge.

Similarly, there are multiple ways to assign dependencies:
```kotlin [build.gradle[.kts]]
stonecutter.dependency("name", "...")
stonecutter.dependency("name") { "..." }

// Kotlin DSL only!
stonecutter.dependency["name"] = "..."
stonecutter.dependencies(
    "name" to "...",
    "other_name" to "..."
)
```

### Swaps
::: code-group
```kotlin [build.gradle.kts]
stonecutter.swap("swap_token") {
    if (stonecutter.compare(stonecutter.current.version, "1.20.1")) "func1()" else "func2()"
}
```

```groovy [build.gradle]
stonecutter.swap("swap_token") {
    stonecutter.compare(stonecutter.current.version, "1.20.1") ? "func1()" : "func2()"
}
```
:::

Swaps have multiple ways to be assigned as well:
```kotlin [build.gradle[.kts]]
stonecutter.swap("name", "...")
stonecutter.swap("name") { "..." }

// Kotlin DSL only!
stonecutter.swap["name"] = "..."
stonecutter.swaps(
    "name" to "...",
    "other_name" to "..."
)
```

### File filters
Stonecutter allows excluding files from the comment processor with `stonecutter.exclude()`.

The first method simply takes a path to the file or directory:
```kotlin [build.gradle[.kts]]
stonecutter.exclude(rootProject.file("src/main/resources/assets/lang"))
```
**Be careful with this method!**. If you use `project.file()`, the start directory will be the corresponding entry in `versions/`,
not where your `stonecutter.gradle[.kts]` is located.

The second method is safer and provides extra functionality.
```kotlin [build.gradle[.kts]]
stonecutter.exclude("src/main/resources/assets/lang")
```
This automatically starts in the directory of `stonecutter.gradle[.kts]`. However, it can also exclude file formats entirely:
```kotlin [build.gradle[.kts]]
stonecutter.exclude("*.json")
```
**`*.` prefix is important for it to differentiate a path from a file extension.**  
By default,
Stonecutter excludes common image and audio formats to avoid occasional byte sequences being recognized as comments.

## Version operations
These are available both in `stonecutter.gradle[.kts]` and `build.gradle[.kts]`, providing utility functions with no side effects.

### `stonecutter.compare()`
> Compares two strings as semantic versions. If either of them can't be parsed - an exception is thrown.
> Returns 1 if the left version is greater, -1 if it's lesser and 0 if the versions are equal.
> 
> For example: `stonecutter.compare("1.20", "1.21") == 0`

### `stonecutter.compareLenient()`
> Works similarly to `stonecutter.compare`, but accepts non-semantic versions.
> If any version is not semantic, they are compared lexicographically (AKA `ab < ac`).

### `stonecutter.eval()`
> Checks the given version against a predicate.
> Predicate has to be one or more entries separated by whitespace. 
> Each entry may have `=, >, <, >=, <=, ~, ^` or no operator.
> If all predicates pass, the check succeeds.  
> If the passed version or any entry can't be parsed - an exception is thrown.
> 
> For example:
> - `stonecutter.eval("1.20", ">=1.21") == false`
> - `stonecutter.eval("1.20+build.1", "1.20") == true` (build metadata is not used in comparisons according to semver spec)
> - `stonecutter.eval("1.20", ">=1.19 <1.21") == true`

### `stonecutter.evalLenient()`
> Works similarly to `stonecutter.eval`, but accepts non-semantic versions.
> If any version is not semantic, they are compared lexicographically (AKA `ab < ac`).