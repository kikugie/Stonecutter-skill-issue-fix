package dev.kikugie.stitchertest

import dev.kikugie.stitcher.exception.ErrorHandlerImpl
import dev.kikugie.stitcher.process.FileParser
import dev.kikugie.stitcher.process.Lexer
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import kotlin.time.measureTime

object Time {
    @Test
    fun test() {
        val input = """
            package dev.kikugie.elytratrims.mixin.client;

import dev.kikugie.elytratrims.mixin.access.ElytraRotationAccessor;
import dev.kikugie.elytratrims.mixin.access.LivingEntityAccessor;
import dev.kikugie.elytratrims.mixin.plugin.MixinConfigurable;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.kikugie.elytratrims.common.util.UtilKt.isProbablyElytra;

@MixinConfigurable
@Mixin(value = SmithingScreen.class, priority = 1100)
public class SmithingScreenMixin implements ElytraRotationAccessor {
    @Unique
    private final Quaternionf dummy = new Quaternionf();
    @Unique
    protected boolean isElytra;
    @Shadow
    @Nullable
    private ArmorStandEntity armorStand;

    @Inject(method = "setup", at = @At("TAIL"))
    private void markGuiArmorStand(CallbackInfo ci) {
        if (armorStand != null) ((LivingEntityAccessor) this.armorStand).elytratrimsmarkGui();
    }

    @Inject(method = "equipArmorStand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), cancellable = true)
    private void equipElytra(ItemStack stack, CallbackInfo ci) {
        if (armorStand == null) return;
        if (isProbablyElytra(stack.getItem())) {
            isElytra = true;
            armorStand.equipStack(EquipmentSlot.CHEST, stack.copy());
            ci.cancel();
        } else isElytra = false;
    }

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE",
            //? if >1.20.4 {
            target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            //?} elif >=1.20.2 {
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;FFILorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            *///?} elif >=1.20.1 {
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            *///?} else
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/util/math/MatrixStack;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"*/
    ), index = /*? if >=1.20.2 {*/ 5 /*?} else {*/ /*4 *//*?}*/)
    private Quaternionf applyRotation(Quaternionf quaternionf) {
        return elytratrimsrotateElytra(quaternionf);
    }

    @Override
    public Quaternionf elytratrimsgetVector() {
        return dummy;
    }

    @Override
    public boolean elytratrimsisElytra() {
        return isElytra;
    }

    @Override
    public void elytratrimssetElytra(boolean value) {
        isElytra = value;
    }
}
        """.trimIndent()
        val input2 = """
            package dev.kikugie.elytratrims.mixin.client;

import dev.kikugie.elytratrims.mixin.access.ElytraRotationAccessor;
import dev.kikugie.elytratrims.mixin.access.LivingEntityAccessor;
import dev.kikugie.elytratrims.mixin.plugin.MixinConfigurable;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.kikugie.elytratrims.common.util.UtilKt.isProbablyElytra;

@MixinConfigurable
@Mixin(value = SmithingScreen.class, priority = 1100)
public class SmithingScreenMixin implements ElytraRotationAccessor {
    @Unique
    private final Quaternionf dummy = new Quaternionf();
    @Unique
    protected boolean isElytra;
    @Shadow
    @Nullable
    private ArmorStandEntity armorStand;

    @Inject(method = "setup", at = @At("TAIL"))
    private void markGuiArmorStand(CallbackInfo ci) {
        if (armorStand != null) ((LivingEntityAccessor) this.armorStand).elytratrimsmarkGui();
    }

    @Inject(method = "equipArmorStand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), cancellable = true)
    private void equipElytra(ItemStack stack, CallbackInfo ci) {
        if (armorStand == null) return;
        if (isProbablyElytra(stack.getItem())) {
            isElytra = true;
            armorStand.equipStack(EquipmentSlot.CHEST, stack.copy());
            ci.cancel();
        } else isElytra = false;
    }

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE",
            //? if >1.20.4 {
            target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            //?} elif >=1.20.2 {
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;FFILorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            *///?} elif >=1.20.1 {
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"
            *///?} else
            /*target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/util/math/MatrixStack;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V"*/
    ), index = /*? if >=1.20.2 {*/ 5 /*?} else {*/ /*4 *//*?}*/)
    private Quaternionf applyRotation(Quaternionf quaternionf) {
        return elytratrimsrotateElytra(quaternionf);
    }

    @Override
    public boolean elytratrimsisElytra() {
        return isElytra;
    }

    @Override
    public void elytratrimssetElytra(boolean value) {
        isElytra = value;
    }
}
        """.trimIndent()
        val time = measureTime {
            val res = input.hash("MD5") == input2.hash("MD5")
        }
        println("Time: ${time.inWholeNanoseconds}")
    }

    private fun createParser(str: String): FileParser {
        return FileParser(str.reader(), recognizers)
    }

    private fun createLexer(str: CharSequence): Lexer {
        val handler = ErrorHandlerImpl(str)
        return Lexer(str, handler)
    }

    private fun String.hash(algorithm: String): String = MessageDigest.getInstance(algorithm).apply {
        this@hash.byteInputStream().use {
            val buffer = ByteArray(1024)
            var read = it.read(buffer)

            while (read != -1) {
                update(buffer, 0, read)
                read = it.read(buffer)
            }
        }
    }.digest().joinToString("") { "%02x".format(it) }
}