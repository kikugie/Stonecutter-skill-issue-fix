# Introduction

## What is Stonecutter?
Stonecutter is a multi-version Gradle plugin, designed to make it possible to maintain Minecraft mods for multiple game versions within a single project.
You may be familiar with [Preprocessor](https://github.com/ReplayMod/preprocessor), which has the same goal. However, Stonecutter has a few significant differences.

## Terminology
Stonecutter splits your project in a versioned setup. 
Versions share a single buildscript, but you can change its behavior by accessing the plugin.

Stonecutter project has a single active version at a time. 
You can launch Minecraft for it, build a `.jar` file and so on. 
However, trying to do it for other versions will result in an error.

When setting up a project, you will have a vcs version, 
which will be your baseline for testing and committing changes.

## Buildscript format
Although using `{}.gradle.kts` files is recommended when working with Stonecutter because
Kotlin DSL provides better IDE integration and tooling, Groovy DSL is supported. 

## Supported files
Stonecutter supports both Java and Kotlin, with support for resource files coming soon.
Although the examples are given primarily in Kotlin for a Fabric mod, the used language and mod loader don't matter for the plugin.