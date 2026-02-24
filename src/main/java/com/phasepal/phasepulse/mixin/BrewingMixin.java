package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.BrewingListener;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player completes brewing a potion.
 */
@Mixin(ScreenHandler.class)
public class BrewingMixin {
    @Inject(method = "onClosed", at = @At("HEAD"))
    private void onClosed(net.minecraft.entity.player.PlayerEntity player, CallbackInfo ci) {
        // When the brewing stand is closed, we check for potions in the output slots.
        // This is a simple heuristic, as true brewing completion happens on the server.
        if ((Object)this instanceof BrewingStandScreenHandler handler) {
            for (int i = 0; i < 3; i++) {
                Slot slot = handler.getSlot(i);
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.PotionItem) {
                    BrewingListener.onPotionBrewed(stack);
                }
            }
        }
    }
}
