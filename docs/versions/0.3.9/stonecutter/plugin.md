## StonecutterSettings

Executed for the `stonecutter` block in `settings.gradle` and responsible for creating versioned subprojects.

### Functions

| Name     | Summary                                                                                                                                                  |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `build`  | `fun build(file: String)` Specifies common buildscript to be used by versions.                                                                           |
| `create` | `fun create(vararg projects: ProjectDescriptor)` Applies Stonecutter to a project, creating `stonecutter.gradle` and applying plugin to the buildscript. |
| `shared` | `fun shared(builder: Action<ProjectBuilder>)` Specifies version directories and initial active version.                                                  |

## StonecutterController

Runs for `stonecutter.gradle` file, applying project configurations to versions and generating versioned tasks.

### Properties

| Name       | Summary                                                           |
|------------|-------------------------------------------------------------------|
| `chiseled` | `val chiseled: Class<ChiseledTask>` Chiseled task type reference. |
| `versions` | `val versions: List` All registered subprojects.                  |

### Functions

| Name               | Summary                                                                                                                                                 |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `active`           | `fun active(str: String)` Sets active Stonecutter version.                                                                                              |
| `debug`            | `fun debug(value: Boolean)` Enables debug functionality. Currently adds `true` and `false` expressions to the processor.                                |
| `registerChiseled` | `fun registerChiseled(provider: TaskProvider)` Registers a chiseled task, which runs in parallel for all versions.                                      |
| `syncProperties`   | `fun syncProperties(value: Boolean)` Reads `gradle.properties` file and adds any property set to `VERSIONED` to subproject's file. Disabled by default. |

## StonecutterBuild

Provides versioned functionality in the buildscript.

### Properties

| Name       | Summary                                                                                          |
|------------|--------------------------------------------------------------------------------------------------|
| `active`   | `val active: ProjectName` Current active version. (Global for all subprojects)                   |
| `current`  | `val current: ProjectVersion` Version of this buildscript instance. (Unique for each subproject) |
| `versions` | `val versions: List<ProjectVersion>` All registered subprojects.                                 |

### Functions

| Name         | Summary                                                                                   |
|--------------|-------------------------------------------------------------------------------------------|
| `expression` | `fun expression(expr: Expression)` Creates a custom expression for the comment processor. |

## ProjectVersion

### Properties

| Name       | Summary                 |
|------------|-------------------------|
| `isActive` | `val isActive: Boolean` |
