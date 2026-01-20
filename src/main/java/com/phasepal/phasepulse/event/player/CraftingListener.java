package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;

/**
 * Listens for item crafted events.
 * Called from CraftingMixin when player crafts an item.
 */
public class CraftingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when player crafts an item.
     * @param craftedItem The item that was crafted
     */
    public static void onItemCrafted(ItemStack craftedItem) {
        if (craftedItem != null && !craftedItem.isEmpty()) {
            String itemType = craftedItem.getItem().toString();
            int count = craftedItem.getCount();

            if (debouncer.shouldTrigger("craft_" + itemType, 500)) {
                EventPacket packet = new EventPacket("item_crafted")
                        .addMetadata("item", itemType)
                        .addMetadata("count", count);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
