## Importing the plugin
In your `settings.gradle` include following fields:
```groovy
pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven {
			name = 'Fabric'
			url = 'https://maven.fabricmc.net/'
		}
		maven {
			url = "https://maven.kikugie.dev/releases"
		}
	}
}

plugins {
	id "dev.kikugie.stonecutter" version "0.3.+"
}

stonecutter {
	shared {
		versions "1.20.2", "1.20.1", "1.19.4" // these specify directories used in `versions`, as well as versions used in comments
		vcsVersion "1.20.2"
	}

	create rootProject
}
```
After reloading `stonecutter.gradle` will be added to the root directory.

## Versioned properties
Create a `versions` directory in your project. For each version specified in `settings.gradle` create a subdirectory with `gradle.properties` file in it.  
Any value specified in version's properties override the global one. Important one is mappings version (yarn or one of your choice) and you will likely need to specify Minecraft and Fabric API overrides.

## Build script changes
After adding Stonecutter `build.gradle` will run for each version.
It shouldn't require many changes, but there are some recommended ones:
```groovy
version = "${property("mod.version")}+${stonecutter.current.version}" // include game version in the mod filename

dependencies {
    minecraft "com.mojang:minecraft:${stonecutter.current.version}" // provide version with stonecutter
}

if (stonecutter.current.isActive) { // run configs for non-active version would be invalid
    loom {
        runConfigs.all {
            ideConfigGenerated = true // generate IDE tasks for running client, server and testmod, datagen if those are present
            runDir "../../run" // use a global run directory for all versions
        }
    }
}
```

## Plugin config
Applied to `stonecutter.gradle` file.
### Chiseled tasks
Chiseled tasks run in parallel for all versions. Example of a chiseled build task:
```groovy
stonecutter.registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    setGroup "build"

    ofTask "build"
}
```