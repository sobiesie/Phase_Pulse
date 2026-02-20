package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.EnchantingListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when player enchants an item at an enchanting table.
 */
@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantingMixin {
    @Shadow @Final public int[] enchantmentPower;

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void afterEnchant(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        // Only trigger if successful, on client side, and it is the local player
        if (cir.getReturnValue() && player == MinecraftClient.getInstance().player && player.getEntityWorld().isClient() && id >= 0 && id < 3) {
            EnchantingListener.onItemEnchanted(enchantmentPower[id]);
        }
    }
}
