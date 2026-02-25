package com.phasepal.phasepulse.mixin;

import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Temporarily disabled while MerchantMenu internals are remapped for NeoForge/Mojmap.
 */
@Mixin(MerchantMenu.class)
public abstract class TradingMixin {
    @Inject(method = "playTradeSound", at = @At("HEAD"))
    private void onTradeSound(CallbackInfo ci) {
    }
}
