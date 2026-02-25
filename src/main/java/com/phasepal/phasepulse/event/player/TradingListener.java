package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.trading.MerchantOffer;

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
    public static void onTradeCompleted(MerchantOffer offer) {
        // Trade notifications are sent when the result slot is taken
        ItemStack output = offer.getResult();
        String outputItem = BuiltInRegistries.ITEM.getKey(output.getItem()).toString().replace("minecraft:", "");
        int count = output.getCount();

        // Debounce to prevent spam from fast trading (500ms cooldown)
        if (!debouncer.shouldTrigger("villager_trade", 500)) {
            return;
        }

        EventPacket packet = new EventPacket("villager_trade")
                .addMetadata("item", outputItem)
                .addMetadata("count", count)
                .addMetadata("experience", offer.getXp());

        NetworkManager.getInstance().sendEvent(packet);
    }
}
