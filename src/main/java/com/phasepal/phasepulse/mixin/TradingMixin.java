package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TradingListener;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player completes a trade with a villager.
 */
@Mixin(MerchantMenu.class)
public abstract class TradingMixin {
	@Shadow
	private MerchantContainer tradeContainer;

	@Inject(method = "playTradeSound", at = @At("HEAD"))
	private void onTradeSound(CallbackInfo ci) {
		if (tradeContainer == null) {
			return;
		}

		MerchantOffer offer = tradeContainer.getActiveOffer();
		if (offer != null) {
			TradingListener.onTradeCompleted(offer);
		}
	}
}
