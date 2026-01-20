package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.CraftingListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player crafts an item.
 */
@Mixin(CraftingResultSlot.class)
public class CraftingMixin {
    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onCraftItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player.getEntityWorld().isClient()) {
            CraftingListener.onItemCrafted(stack);
        }
    }
}
