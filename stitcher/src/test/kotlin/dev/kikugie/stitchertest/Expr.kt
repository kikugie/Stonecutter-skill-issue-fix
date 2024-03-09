package dev.kikugie.stitchertest

import dev.kikugie.stitcher.assembler.AssemblyVisitor
import dev.kikugie.stitcher.transformer.ASTTransformer
import dev.kikugie.stitcher.transformer.ExpressionProcessor
import dev.kikugie.stitcher.transformer.SwapProcessor
import dev.kikugie.stitchertest.util.parse
import dev.kikugie.stitchertest.util.printCol
import dev.kikugie.stitchertest.util.recognizers
import dev.kikugie.stitchertest.util.yaml
import org.junit.jupiter.api.Test

object Expr {
    @Test
    fun test() {
        val input = """
            //$ token
            aaa
        """.trimIndent()
        val processor = ExpressionProcessor(listOf {
            it == "true"
        })
        val swaps = SwapProcessor.Builder().apply {
            this["token"] = {
                if (it.test("false"))
                    "aaa"
                else
                    "bbb"
            }
        }
        val ast = input.parse()
        println("Original")
        printCol(ast.yaml())
        val transformer = ASTTransformer(ast, recognizers, processor, swaps)
        transformer.process()
        println("Transformed")
        printCol(ast.yaml())
        val res = AssemblyVisitor.visitScope(ast)
        printCol(res)
    }

}