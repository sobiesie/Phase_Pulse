package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TradingListener;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player completes a trade with a villager.
 */
@Mixin(MerchantScreenHandler.class)
public abstract class TradingMixin {
    @Shadow private MerchantInventory merchantInventory;

    @Inject(method = "playYesSound", at = @At("HEAD"))
    private void onTradeSound(CallbackInfo ci) {
        // In 1.21.11, the active trade is tracked in MerchantInventory, not as a recipe index field.
        if (merchantInventory != null) {
            TradeOffer offer = merchantInventory.getTradeOffer();
            if (offer != null) {
                TradingListener.onTradeCompleted(offer);
            }
        }
    }
}
