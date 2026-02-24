package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.village.TradeOffer;

/**
 * Handles villager trading events.
 * Called from VillagerTradeMixin when a trade is completed.
 */
public class TradingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when a player completes a trade with a villager.
     * @param offer The trade offer completed
     */
    public static void onTradeCompleted(TradeOffer offer) {
        // Trade notifications are sent when the result slot is taken
        ItemStack output = offer.getSellItem();
        String outputItem = Registries.ITEM.getId(output.getItem()).toString().replace("minecraft:", "");
        int count = output.getCount();

        // Debounce to prevent spam from fast trading (500ms cooldown)
        if (!debouncer.shouldTrigger("villager_trade", 500)) {
            return;
        }

        EventPacket packet = new EventPacket("villager_trade")
                .addMetadata("item", outputItem)
                .addMetadata("count", count)
                .addMetadata("experience", offer.getMerchantExperience());

        NetworkManager.getInstance().sendEvent(packet);
    }
}
