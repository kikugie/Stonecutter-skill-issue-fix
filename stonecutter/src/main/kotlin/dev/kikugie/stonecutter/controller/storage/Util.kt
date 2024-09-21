package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.ProjectName
import dev.kikugie.stonecutter.data.ProjectContainer

internal operator fun ProjectBranch?.get(project: ProjectName) =
    this?.nodes?.get(project)

open class ProjectTreeContainer : ProjectContainer<ProjectTree>()
open class ProjectParameterContainer : ProjectContainer<GlobalParameters>()