package dev.kikugie.stitchertest

import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Transformer
import dev.kikugie.stitchertest.util.*
import org.junit.jupiter.api.Test
import java.nio.file.StandardOpenOption
import kotlin.io.path.*


object Expr {
//    @Test
//    fun test() {
//        val input = """
//            //$ token
//            aaa
//        """.trimIndent()
//        val processor = ExpressionProcessor(listOf {
//            it == "true"
//        })
//        val swaps = SwapProcessor.Builder().apply {
//            this["token"] = {
//                if (it.test("false"))
//                    "aaa"
//                else
//                    "bbb"
//            }
//        }
//        val ast = input.parse()
//        println("Original")
//        printCol(ast.yaml())
//        val transformer = ASTTransformer(ast, recognizers, processor, swaps, TransformerSettings.DEFAULT)
//        transformer.process()
//        println("Transformed")
//        printCol(ast.yaml())
//        val res = Assembler.visitScope(ast)
//        printCol(res)
//    }

    @Test
    fun test2() {
        val input = Path("E:\\IdeaProjects\\Stonecutter-skill-issue-fix\\stitcher\\src\\test\\kotlin\\dev\\kikugie\\stitchertest\\test.kt").readText()
        val ast = input.parse()
//        printCol(Assembler.visitScope(ast))
        val y1 = ast.yaml()
        printCol(y1)
//
        val transformer = Transformer.create(ast, recognizers, constants = mapOf("true" to true, "false" to false))
        transformer.process()

        val y2 = ast.yaml()
        val res = Assembler.visitScope(ast)
        Path("E:\\IdeaProjects\\Stonecutter-skill-issue-fix\\stitcher\\src\\test\\kotlin\\dev\\kikugie\\stitchertest\\res\\test-res.kt").writeText(res,
            options = arrayOf(StandardOpenOption.CREATE)
        )
    }

}