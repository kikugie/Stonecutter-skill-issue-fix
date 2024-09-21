package dev.kikugie.stonecutter.intellij.util

import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.transformer.ConditionChecker
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.parser.CommentParser
import dev.kikugie.stonecutter.StonecutterProject
import dev.kikugie.experimentalstonecutter.build.StonecutterData
import dev.kikugie.stonecutter.process.toParams

fun String.parse(): Definition? {
    val lexer = Lexer(this)
    val parser = CommentParser(lexer, StoringErrorHandler())
    return parser.parse()
}

fun String.findMatching(models: Map<StonecutterProject, StonecutterData>): Collection<StonecutterProject> {
    val def = parse()?.takeIf(Definition::isNotEmpty) ?: return emptyList()
    return when {
        def.swap != null -> models.keys
        def.condition != null -> models.mapNotNull { (k, v) ->
            val visitor = ConditionChecker(v.toParams(k.version))
            val pass = runCatching { def.accept(visitor) }.getOrElse { false }
            if (pass) k else null
        }
        else -> emptyList()
    }
}