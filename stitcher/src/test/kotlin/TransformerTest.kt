import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.red
import dev.kikugie.stitcher.eval.join
import dev.kikugie.stitcher.exception.StoringErrorHandler
import dev.kikugie.stitcher.parser.FileParser
import dev.kikugie.stitcher.scanner.CommentRecognizers
import dev.kikugie.stitcher.transformer.TransformParameters
import dev.kikugie.stitcher.transformer.TransformParameters.Companion.TransformParameters
import dev.kikugie.stitcher.transformer.Transformer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

object TransformerTest {
    val SAMPLES = buildMap {
        add("basic swap") {
            input = """
                //$ swap
                placeholder1
            """.trimIndent()
            expected = """
                //$ swap
                placeholder2
            """.trimIndent()
            params {
                swaps["swap"] = "placeholder2"
            }
        }
        add("swap tab indent") {
            input = "//\$ swap\n\tplaceholder1"
            expected = "//\$ swap\n\tplaceholder2"
            params {
                swaps["swap"] = "placeholder2"
            }
        }
        add("swap tab") {
            input = """
                //$ swap
                    placeholder1
            """.trimIndent()
            expected = """
                //$ swap
                    placeholder2
            """.trimIndent()
            params {
                swaps["swap"] = "placeholder2"
            }
        }

        add("closed condition - add comment") {
            input = """
                //? if const {
                placeholder
                //?}
            """.trimIndent()
            expected =  """
                //? if const {
                /*placeholder
                *///?}
            """.trimIndent()
            params {
                constants["const"] = false
            }
        }
        add("closed condition - remove comment") {
            input = """
                //? if const {
                /*placeholder
                *///?}
            """.trimIndent()
            expected =  """
                //? if const {
                placeholder
                //?}
            """.trimIndent()
            params {
                constants["const"] = true
            }
        }

        add("line condition - add comment") {
            input = """
                //? if const
                placeholder
                other
            """.trimIndent()
            expected =  """
                //? if const
                /*placeholder*/
                other
            """.trimIndent()
            params {
                constants["const"] = false
            }
        }
        add("line condition - remove comment") {
            input = """
                //? if const
                /*placeholder*/
                other
            """.trimIndent()
            expected =  """
                //? if const
                placeholder
                other
            """.trimIndent()
            params {
                constants["const"] = true
            }
        }

        add("word condition - add comment") {
            input = """
                //? if const >>
                placeholder other
            """.trimIndent()
            expected =  """
                //? if const >>
                /*placeholder*/ other
            """.trimIndent()
            params {
                constants["const"] = false
            }
        }
        add("word condition - remove comment") {
            input = """
                //? if const >>
                /*placeholder*/ other
            """.trimIndent()
            expected =  """
                //? if const >>
                placeholder other
            """.trimIndent()
            params {
                constants["const"] = true
            }
        }

        add("nested conditions - add comment") {
            input = """
                //? if const {
                placeholder
                    /*? if const2*/
                    placeholder
                //?}
            """.trimIndent()
            expected =  """
                //? if const {
                /*placeholder
                    /^? if const2^/
                    placeholder
                *///?}
            """.trimIndent()
            params {
                constants["const"] = false
                constants["const2"] = false
            }
        }
        add("nested conditions - remove comment") {
            input = """
                //? if const {
                /*placeholder
                    /^? if const2^/
                    placeholder
                *///?}
            """.trimIndent()
            expected =  """
                //? if const {
                placeholder
                    /*? if const2*/
                    placeholder
                //?}
            """.trimIndent()
            params {
                constants["const"] = true
                constants["const2"] = true
            }
        }
        add("nested conditions - recursive parsing") {
            input = """
                //? if const {
                /*placeholder
                    /^? if const2^/
                    placeholder
                *///?}
            """.trimIndent()
            expected =  """
                //? if const {
                placeholder
                    /*? if const2*/
                    /*placeholder*/
                //?}
            """.trimIndent()
            params {
                constants["const"] = true
                constants["const2"] = false
            }
        }

        add("closed condition - comment handling") {
            input = """
                //? if const {
                placeholder
                // comment
                //?}
            """.trimIndent()
            expected =  """
                //? if const {
                /*placeholder
                // comment
                *///?}
            """.trimIndent()
            params {
                constants["const"] = false
            }
        }

        add("closed condition - else") {
            input = """
                //? if const {
                placeholder
                //?} else {
                /*placeholder2
                *///?}
            """.trimIndent()
            expected =  """
                //? if const {
                /*placeholder
                *///?} else {
                placeholder2
                //?}
            """.trimIndent()
            params {
                constants["const"] = false
            }
        }
        /**Nested comment bug provided by embeddedt*/
        add("multinest test") {
            input = """
                package org.embeddedt.embeddium.impl.mixin.features.render.immediate.buffer_builder.sorting;

                //? if >=1.18 <1.21 {

                /*@Mixin(BufferBuilder.class)
                public abstract class BufferBuilderMixin {

                    //? if >=1.20 {
                    
                    @Overwrite
                    private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
                        if (this.sorting != null) {
                            int[] indices = this.sorting.sort(this.sortingPoints);
                            this.writePrimitiveIndices(indexType, indices);
                        }
                    }
                    //?} else {
                    /^@Inject(method = "putSortedQuadIndices", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;intConsumer(" + /^? if >=1.19 {^/ "I" + /^?}^/ "Lcom/mojang/blaze3d/vertex/VertexFormat}${'$'}IndexType;)Lit/unimi/dsi/fastutil/ints/IntConsumer;"), cancellable = true)
                    private void putSortedQuadIndices(VertexFormat.IndexType indexType, CallbackInfo ci, @Local(ordinal = 0) int[] indices) {
                        ci.cancel();
                        this.writePrimitiveIndices(indexType, indices);
                    }
                    ^///?}
                }

                *///?}
            """.trimIndent()
            expected = """
                package org.embeddedt.embeddium.impl.mixin.features.render.immediate.buffer_builder.sorting;

                //? if >=1.18 <1.21 {

                @Mixin(BufferBuilder.class)
                public abstract class BufferBuilderMixin {

                    //? if >=1.20 {
                    
                    @Overwrite
                    private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
                        if (this.sorting != null) {
                            int[] indices = this.sorting.sort(this.sortingPoints);
                            this.writePrimitiveIndices(indexType, indices);
                        }
                    }
                    //?} else {
                    /*@Inject(method = "putSortedQuadIndices", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;intConsumer(" + /^? if >=1.19 {^/ "I" + /^?}^/ "Lcom/mojang/blaze3d/vertex/VertexFormat}${'$'}IndexType;)Lit/unimi/dsi/fastutil/ints/IntConsumer;"), cancellable = true)
                    private void putSortedQuadIndices(VertexFormat.IndexType indexType, CallbackInfo ci, @Local(ordinal = 0) int[] indices) {
                        ci.cancel();
                        this.writePrimitiveIndices(indexType, indices);
                    }
                    *///?}
                }

                //?}
            """.trimIndent()
            params {
                dependencies[""] = "1.20"
            }
        }
    }

    @TestFactory
    fun `test transformer`() = SAMPLES.map {
        DynamicTest.dynamicTest(it.key) { check(it.value) }
    }

    private fun check(data: Data) {
        val handler = StoringErrorHandler()
        fun checkHandlerErrors() = Assertions.assertIterableEquals(emptyList<String>(), handler.errors) {
            val errors = handler.errors.joinToString {
                "- ${red(it.second)} - ${cyan(it.first.toString())}"
            }
            "Unexpected errors:\n$errors\n"
        }

        val parser = FileParser(data.input.scan(), data.params, handler)
        val scope = parser.parse()
        scope.yaml().lines().forEach { println(cyan(it)) }
        assertNotNull(scope)
        checkHandlerErrors()

        val transformer = Transformer(scope, CommentRecognizers.DEFAULT, data.params, handler)
        val modified = transformer.process()
        checkHandlerErrors()

        Assertions.assertEquals(data.expected, modified.join())
    }

    private inline fun MutableMap<String, Data>.add(name: String, build: Data.() -> Unit) {
        put(name, Data().apply(build))
    }

    class Data {
        lateinit var expected: String
        lateinit var input: String
        lateinit var params: TransformParameters
        inline fun params(build: TransformParameters.TransformParametersBuilder.() -> Unit) {
            params = TransformParameters(build)
        }
    }
}