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

::: code-group
```kotlin [tree builder (Kotlin)]
versions("1.20.1", "1.21")
branch("subproject") // Copies versions from the main branch
```
```groovy [tree builder (Groovy)]
versions "1.20.1", "1.21"
branch "subproject" // Copies versions from the main branch
```
:::
::: code-group
```kotlin [tree builder (Kotlin)]
branch("subproject") {
    versions("1.19.4", "1.20.1") // Specifies a separate version set
}
```
```groovy [tree builder (Groovy)]
branch("subproject", branch -> {
    branch.versions "1.19.4", "1.20.1" // Specifies a separate version set
})
```
:::
::: code-group
```kotlin [tree builder (Kotlin)]
versions("1.20.1", "1.21")
branch("subproject") {
    buildscript = "subproject.gradle.kts"
    inherit() // Copies versions from the main branch
}
```
```groovy [tree builder (Groovy)]
versions "1.20.1", "1.21"
branch("subproject", branch -> {
    branch.buildscript = "subproject.gradle.kts"
    branch.inherit() // Copies versions from the main branch
})
```
:::

## To be continued