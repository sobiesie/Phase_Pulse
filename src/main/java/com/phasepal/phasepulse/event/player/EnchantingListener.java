package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;

/**
 * Listens for item enchanted events.
 * Called from EnchantingMixin when player enchants an item.
 */
public class EnchantingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when player successfully enchants an item.
     * @param levelCost The level cost of the enchantment (1, 2, or 3)
     */
    public static void onItemEnchanted(int levelCost) {
        if (debouncer.shouldTrigger("enchant", 500)) {
            EventPacket packet = new EventPacket("item_enchanted")
                    .addMetadata("level_cost", levelCost);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
