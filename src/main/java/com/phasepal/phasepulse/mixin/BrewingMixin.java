package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.BrewingListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player completes brewing a potion.
 */
@Mixin(AbstractContainerMenu.class)
public class BrewingMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void onClosed(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        // When the brewing stand is closed, we check for potions in the output slots.
        // This is a simple heuristic, as true brewing completion happens on the server.
        if ((Object)this instanceof BrewingStandMenu handler) {
            for (int i = 0; i < 3; i++) {
                Slot slot = handler.getSlot(i);
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.PotionItem) {
                    BrewingListener.onPotionBrewed(stack);
                }
            }
        }
    }
}
