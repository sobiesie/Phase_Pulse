package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.AnvilListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player uses the anvil (takes output).
 */
@Mixin(AnvilScreenHandler.class)
public class AnvilMixin {
    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void onAnvilOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // Only trigger if it is the local player and on client side
        if (player == MinecraftClient.getInstance().player && player.getEntityWorld().isClient()) {
            AnvilListener.onAnvilUsed(stack);
        }
    }
}
