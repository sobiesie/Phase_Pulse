package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.SmeltingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.FurnaceResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player takes smelted items from furnace/blast furnace/smoker.
 */
@Mixin(FurnaceResultSlot.class)
public class SmeltingMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void onSmeltedItemTaken(Player player, ItemStack stack, CallbackInfo ci) {
        // Only trigger if it is the local player and on client side
        if (player == Minecraft.getInstance().player && player.level().isClientSide()) {
            SmeltingListener.onItemSmelted(stack);
        }
    }
}
