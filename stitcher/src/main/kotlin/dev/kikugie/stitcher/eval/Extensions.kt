package dev.kikugie.stitcher.eval

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.component.Component
import dev.kikugie.stitcher.data.scope.Scope
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.transformer.TransformParameters

fun Token.isEmpty() = this == Token.EMPTY
fun Token.isNotEmpty() = !isEmpty()

fun Token.isBlank() = value.isBlank()
fun Token.isNotBlank() = !isBlank()

fun Block.isEmpty() = accept(EmptyChecker)
fun Block.isNotEmpty() = !isEmpty()

fun Component.isEmpty() = accept(EmptyChecker)
fun Component.isNotEmpty() = !isEmpty()

fun Block.isBlank() = accept(Assembler).isBlank()
fun Block.isNotBlank() = !isBlank()

fun Component.isBlank() = accept(Assembler).isBlank()
fun Component.isNotBlank() = !isBlank()

fun Scope.join() = accept(Assembler)
fun Block.join() = accept(Assembler)
fun Component.join() = accept(Assembler)
fun Component.eval(params: TransformParameters) = accept(ConditionChecker(params))