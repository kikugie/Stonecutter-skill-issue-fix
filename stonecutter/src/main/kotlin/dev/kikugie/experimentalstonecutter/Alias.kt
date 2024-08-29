package dev.kikugie.experimentalstonecutter

import org.slf4j.LoggerFactory

typealias ProjectName = String
typealias ProjectPath = String
typealias TargetVersion = String
typealias TaskName = String

internal val BNAN = "🍌"

internal fun ProjectPath.sanitize() = removePrefix(":")