package dev.kikugie.stitchertest

import dev.kikugie.stitcher.process.Assembler
import dev.kikugie.stitcher.process.Lexer.Companion.lex
import dev.kikugie.stitcher.process.Scanner.Companion.scan
import dev.kikugie.stitcher.process.Transformer.Companion.transform
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream

object Time {
    @Test
    fun test() {
        val input = """
            package dev.kikugie.elytratrims.common

            import dev.kikugie.elytratrims.common.access.FeatureAccess.getAnimationStatus
            import dev.kikugie.elytratrims.common.access.FeatureAccess.getColor
            import dev.kikugie.elytratrims.common.access.FeatureAccess.getPatterns
            import dev.kikugie.elytratrims.common.access.FeatureAccess.hasGlow
            import dev.kikugie.elytratrims.common.access.FeatureAccess.removeAnimationStatus
            import dev.kikugie.elytratrims.common.access.FeatureAccess.removeColor
            import dev.kikugie.elytratrims.common.access.FeatureAccess.removeGlow
            import dev.kikugie.elytratrims.common.access.FeatureAccess.removePatterns
            import dev.kikugie.elytratrims.common.config.ETServerConfig
            import dev.kikugie.elytratrims.platform.ModStatus
            import net.minecraft.block.LeveledCauldronBlock
            import net.minecraft.block.cauldron.CauldronBehavior
            import net.minecraft.item.Items
            import net.minecraft.stat.Stats

            object ETCommon {
                @JvmField
                val config: ETServerConfig = if (ModStatus.isClient) ETServerConfig.create() else ETServerConfig.load()

                fun init() {
                    ETCommentary.run()
                    if (!config.cleanableElytra) return
                    val behaviour = CauldronBehavior { state, world, pos, player, _, stack ->
                        var glowRemoval = false
                        var bannerRemoval = false
                        var dyeRemoval = false
                        var animationRemoval = false

                        if (stack.hasGlow()) {
                            stack.removeGlow()
                            glowRemoval = true
                        }
                        if (stack.getColor() != 0) {
                            stack.removeColor()
                            dyeRemoval = true
                        }
                        if (stack.getPatterns().isNotEmpty()) {
                            stack.removePatterns()
                            bannerRemoval = true
                        }
                        if (stack.getAnimationStatus()) {
                            stack.removeAnimationStatus()
                            animationRemoval = true
                        }
                        val result = if (glowRemoval || bannerRemoval || dyeRemoval || animationRemoval) {
                            player.incrementStat(Stats.CLEAN_ARMOR)
                            LeveledCauldronBlock.decrementFluidLevel(state, world, pos)
                            true
                        } else false
                        /*? if <=1.20.4 {*/
                        return@CauldronBehavior if (result) net.minecraft.util.ActionResult.success(world.isClient)
                        else net.minecraft.util.ActionResult.PASS
                        /*?} else {*//*
                        return@CauldronBehavior if (result) net.minecraft.util.ItemActionResult.success(world.isClient)
                        else net.minecraft.util.ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                        *//*?}*/
                    }
                    // TODO delegate this to the point when all mods are loaded
                    CauldronBehavior.WATER_CAULDRON_BEHAVIOR/*? if >1.20.2*//*.map()*/
                        .put(Items.ELYTRA, behaviour)
                }
            }
        """.trimIndent()
        val transformed = input.reader().scan(recognizers).lex().parse().transform(recognizers, constants = mapOf(
            ">1.20.2" to false,
            "<=1.20.4" to false,
        ))
        println(transformed.accept(Assembler))
    }

    fun Path.hash(algorithm: String): String {
        return MessageDigest.getInstance(algorithm).apply {
            this@hash.inputStream().use {
                val buffer = ByteArray(1024)
                var read = it.read(buffer)

                while (read != -1) {
                    update(buffer, 0, read)
                    read = it.read(buffer)
                }
            }
        }.digest().joinToString("") { "%02x".format(it) }
    }
}