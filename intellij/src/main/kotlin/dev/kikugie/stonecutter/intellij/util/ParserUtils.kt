package dev.kikugie.stonecutter.intellij.util

import com.intellij.openapi.module.Module
import dev.kikugie.stitcher.data.component.Definition
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.lexer.Lexer
import dev.kikugie.stitcher.parser.CommentParser
import dev.kikugie.stitcher.eval.ConditionChecker
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stonecutter.configuration.StonecutterModel
import dev.kikugie.stonecutter.process.toParams

fun String.parse(model: StonecutterModel): Definition? {
    val lexer = Lexer(this)
    val parser = CommentParser(lexer, StoringErrorHandler(), model.data.toParams(model.current.version))
    return parser.parse()
}

fun String.parse(): Definition? {
    val lexer = Lexer(this)
    val parser = CommentParser(lexer, StoringErrorHandler())
    return parser.parse()
}

fun String.findMatching(module: Module): List<StonecutterModel> {
    val service = module.stonecutterService ?: return emptyList()
    val def = parse()
        ?.takeIf { it.type == MarkerType.CONDITION }
        ?: return emptyList()
    return service.models.values.filter {
        val visitor = ConditionChecker(it.data.toParams(it.current.version))
        visitor.visitDefinition(def)
    }
}