package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.SmeltingListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player takes smelted items from furnace/blast furnace/smoker.
 */
@Mixin(FurnaceOutputSlot.class)
public class SmeltingMixin {
    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onSmeltedItemTaken(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // Only trigger if it is the local player and on client side
        if (player == MinecraftClient.getInstance().player && player.getEntityWorld().isClient()) {
            SmeltingListener.onItemSmelted(stack);
        }
    }
}
