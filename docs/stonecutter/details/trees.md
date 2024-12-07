# Project trees

Project trees create multiple versioned subprojects and provide an interface for
managing them.

## Basics
Trees allow only three layers of depth: `root - branch - node`.
Root contains `stonecutter.gradle[.kts]` and branches contain sets of nodes
that are built into multiple jars.

Even though it is possible to create multiple versioned projects in one codebase,
project trees provide a few important features:
1. Version synchronisation
    Branches in project trees are guaranteed to have the same active version.
2. Project dependencies
    Gradle subprojects are often bundled into the main one or depend on it and distributed separately.
    Project trees ensure dependency on the correct versioned subproject.
3. Project properties
    Project tree nodes can access Gradle properties from other branches, reducing repetition.
4. Supporting inconsistent version sets
    Branches are allowed to have different version sets, in which case some may not be active
    depending on the active version. Project trees ensure the source code is processed even for
    inactive versions.

A tree is created for every Stonecutter project. The setup showcased in the guide section creates
a tree with just one branch.

## Building a tree
Project trees are built in `settings.gradle[.kts]`.
```kotlin
stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.1")
    }
}
```
`create(rootProject)` initializes a tree in the project's root directory.
`versions("1.19.4", "1.20.1", "1.21.1")` creates the root branch and assigns nodes to it.

### Adding branches
Branches can be added with the `branch()` method:
```kotlin
stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.1")
        branch("api")
    }
}
```
This creates an `api` branch and copies the version set from the root.
Root's versions must be declared before branches are added.

### Providing versions
Branches can have a different set of versions than the root.
The function can be expanded with a lambda, where the same `vers()` and `versions()`
methods are available.

::: warning Groovy notice
Due to Groovy having issues with nested function calls,
the `branch()` configuration works differently.
Please check the corresponding code tab.
:::

::: code-group
```kotlin [settings.gradle.kts]
stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.1")
        branch("api") {
            versions("1.20.1", "1.21.1")
        }
    }
}
```
```groovy [settings.gradle]
stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.1")
        branch("api", branch -> {
            branch.versions("1.20.1", "1.21.1")
        })
    }
}
```
:::

### Excluding the root branch
The root branch is only created if there are versions declared for it.
You can avoid it entirely if every other branch is provided its own set.

::: code-group
```kotlin [settings.gradle.kts]
val versions = listOf("1.19.4", "1.20.1", "1.21.1")
stonecutter {
    create(rootProject) {
        branch("main") {
            versions(versions)
        }
        branch("api") {
            versions(versions)
        }
    }
}
```
```groovy [settings.gradle]
def versions = ["1.19.4", "1.20.1", "1.21.1"]
stonecutter {
    create(rootProject) {
        branch("main", branch -> {
            branch.versions(versions)
        })
        branch("api", branch -> {
            branch.versions(versions)
        })
    }
}
```
:::

## Managing the tree
The project tree is primarily used in `stonecutter.gradle[.kts]`,
where it is transformed to be more user-accessible.

### Tree structure
The tree object can be accessed with `stonecutter.tree`.

It inherits `Map<String, ProjectBranch>` and `Project` interfaces,
which allows directly accessing branches and project properties.

Branches implement `Map<String, ProjectNode>` and `Project` for the same
advantages and have an `id` parameter, which is used as a key in the project tree.  
Ids match the ones set in `settings.gradle[.kts]`, however the root branch always
has the id of an empty string.

Nodes implement the `Project` interface and provide a few useful methods
described in the next section.

### Accessing nodes
Nodes can be used to reliably access other nodes in `build.gradle[.kts]`.
The available methods are:
- `node.peer(node)` finds a node with the given name in the current branch.
- `node.sibling(branch)` finds a node with the same name in the given branch.
- `node.find(branch, node)` finds a node with the given branch and name.

Some common use cases are:
::: details Accessing common properties
In a multi-loader project you may need to access properties of the common subproject.
You can get the correct versioned variant using `node.sibling()`.
```kotlin
val common: Project = stonecutter.node.sibling("common")
val myProperty: String = common.property("my_property") as String
```
:::
::: details Depending on a versioned subproject
This example uses Fabric Loom to depend on a subproject and bundle it into the mod.
```kotlin
dependencies {
    val apiProject = stonecutter.node.sibling("api")
    implementation(project(path = apiProject.getPath(), configuration = "namedElements"))
    include(project(apiProject.getPath()))
}
```
:::