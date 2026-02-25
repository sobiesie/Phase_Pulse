package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.CraftingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player crafts an item.
 */
@Mixin(ResultSlot.class)
public class CraftingMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void onCraftItem(Player player, ItemStack stack, CallbackInfo ci) {
        // Only trigger if it is the local player and on client side
        if (player == Minecraft.getInstance().player && player.level().isClientSide()) {
            CraftingListener.onItemCrafted(stack);
        }
    }
}
