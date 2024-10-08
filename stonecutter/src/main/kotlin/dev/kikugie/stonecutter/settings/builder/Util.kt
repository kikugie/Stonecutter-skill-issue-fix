package dev.kikugie.stonecutter.settings.builder

import dev.kikugie.stonecutter.StonecutterProject

internal fun TreeBuilder.treeView() = buildString {
    appendLine("|- vcs: $vcsVersion")
    appendLine("|- versions:")
    appendLine(versions.values.treeView().prepend("| "))
    appendLine("\\- branches:")
    append(nodes.treeView().prepend("  "))
}

internal fun String.prepend(str: String) = lines().joinToString("\n") { str + it }

internal fun StonecutterProject.treeView() = "- $project: v$version"

internal fun Collection<StonecutterProject>.treeView() =
    map { it.treeView() }.mapIndexed { j, line ->
        val lastLine = j == size - 1
        if (!lastLine) line.prepend("|")
        else line.prepend(" ").replaceFirst(' ', '\\')
    }.joinToString("\n")

internal fun NodeMap.treeView() = entries.mapIndexed { i, entry ->
    val (name, nodes) = entry
    val joined = nodes.treeView()
    val lastElem = i == this@treeView.size - 1
    val elem = if (lastElem) joined.prepend("  ")
    else joined.prepend("| ")
    val header = if (lastElem) "\\- $name:\n"
    else "|- $name:\n"
    header + elem
}.joinToString("\n")
