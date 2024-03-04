package dev.kikugie.stitcher.assembler

import dev.kikugie.stitcher.parser.Scope

fun assemble(input: Scope)= buildString {
    input.blocks.forEach {
        append(AssemblyVisitor.visitBlock(it))
    }
}