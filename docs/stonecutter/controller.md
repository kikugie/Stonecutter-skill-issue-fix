# Project controller

Project controller (`stonecutter.gradle[.kts]`) used globally for all versions
and provides additional configuration.

## Property overview
### `stonecutter.tree`
> Allows traversing the project tree constructed in `settings.gradle[.kts]`.
> See the [Project Tree](/stonecutter/project-tree) page for more information.

### `stonecutter.vcsVersion`
> Gets the `vcsVersion` specified in `settings.gradle[.kts]`.

### `stonecutter.versions`
> Returns a set of all registered version metadata.
> This is a union of all branches' versions, so it may contain more entries than some branches.

### `stonecutter.current`
> Returns the version metadata selected to be active by the `stonecutter.active "..."` call.

## Chiseled tasks
Chiseled tasks configure Stonecutter to process comments for all versions and use the changed files to build the project.
::: code-group
```kts [stonecutter.gradle.kts]
stonecutter registerChiseled tasks.register("chiseled...", stonecutter.chiseled) {
    ofTask("...")
}
```

```groovy [stonecutter.gradle]
stonecutter.registerChiseled tasks.register("chiseled...", stonecutter.chiseled) {
    ofTask "..."
}
```
:::
You can use these to collect build files or publish mod updates.

## Version parameters
`stonecutter.parameters { }` are used to configure the default comment processor parameters.
The scope has all methods available on the [Project Build](/stonecutter/build) page.

In the parameter scope you receive `branch`, `metadata` and `node` properties.
Branch and node references can be used to traverse the project tree and access the assigned Gradle projects.
See the [Project Tree](/stonecutter/project-tree) page for more information.

In a simple Stonecutter project, you can use this function to configure additional Stonecutter parameters in one place,
but it's more useful when you have branches with different version sets.
This function runs on all combinations of branches and version ids, which means it may point to a non-registered version.
You still need to provide parameters in that case to make sure version switching works correctly.

## To be continued