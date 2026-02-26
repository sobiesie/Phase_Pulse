package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.AdvancementListener;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when player earns an advancement (achievement) on client-side.
 * Hooks into the toast notification system to detect advancement completion.
 */
@Mixin(ToastComponent.class)
public class AdvancementMixin {
    @Inject(method = "addToast", at = @At("HEAD"))
    private void onToastAdded(net.minecraft.client.gui.components.toasts.Toast toast, CallbackInfo ci) {
        // Check if this is an advancement toast
        if (toast instanceof AdvancementToast advancementToast) {
            // Use reflection or accessor to get the advancement from the toast
            // For now, we'll call the listener which can try to extract the info
            AdvancementListener.onAdvancementToastShown(advancementToast);
        }
    }
}
