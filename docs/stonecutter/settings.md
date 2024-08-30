# Project settings

This section covers advanced project configurations, such as multi-loader and multi-project setups.  
In the following sections you'll encounter mentions of the "tree builder", which refers to the following scopes:
```kotlin
shared {
    // tree builder
}
create(rootProject)
```
```kotlin
create(rootProject) {
    // tree builder
}
```

## Project tree
Stonecutter organizes all projects into a simple tree structure consisting of branches and nodes.  
Standard projects have only one branch named `""`, referred as the main branch.

Using `versions()` and `vers()` inside the tree builder adds them to the main branch.
Additional branches can be added using the `branch()` function.

```kotlin [tree builder]
versions("1.20.1", "1.21")
branch("subproject") // Copies versions from the main branch
```

```kotlin [tree builder]
branch("subproject") {
    versions("1.19.4", "1.20.1") // Specifies a separate version set
}
```

```kotlin [tree builder]
versions("1.20.1", "1.21")
branch("subproject") {
    buildscript = "subproject.gradle.kts"
    inherit() // Copies versions from the main branch
}
```

## To be continued