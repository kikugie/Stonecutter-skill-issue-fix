# Running active version
**DO NOT** launch it from `Tasks -> fabric -> runClient` as it won't include your mod.  
To launch the active version use `Tasks -> <active version> -> fabric -> runClient`.

To make it easier to manage include this snippet in your `build.gradle.kts` (for Groovy, adjust accordingly):
```kt
loom {
    runConfigs["client"].apply {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}
```
This will make loom generate run tasks for all subversions, but you still need to make sure you select the active one.

To make it less error prone you can include this code in the buildscript:
```kt
if (stonecutter.current.isActive) {
    rootProject.tasks.register("runActive") {
        group = "project"

        dependsOn(tasks.named("runClient"))
    }
}
```
This will add `Tasks -> project -> runActive`, which will always run the active project.

# Switching the active version
Use tasks in `Tasks -> stonecutter` to change the active version. This will apply the correct dependencies and process your files, which is explained in the following sections.

# Resetting the active version
`Tasks -> stonecutter -> Reset active version` switches the active version to the `vcsVersion` parameter in `settings.gradle`. It's recommended to run this task before commits either manually or via a commit hook.