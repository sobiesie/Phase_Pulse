package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.EnchantingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when player enchants an item at an enchanting table.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantingMixin {
    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void afterEnchant(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        // Only trigger if successful, on client side, and it is the local player
        if (cir.getReturnValue() && player == Minecraft.getInstance().player && player.level().isClientSide() && id >= 0 && id < 3) {
            EnchantingListener.onItemEnchanted(id + 1);
        }
    }
}
