package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/**
 * Handles fishing events.
 */
public class FishingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when a player catches an item through fishing.
     * @param itemStack The item caught
     */
    public static void onItemCaught(ItemStack itemStack) {
        String itemId = Registries.ITEM.getId(itemStack.getItem()).toString().replace("minecraft:", "");
        int count = itemStack.getCount();

        if (debouncer.shouldTrigger("fishing_catch", 1000)) {
            EventPacket packet = new EventPacket("fishing_catch")
                    .addMetadata("item", itemId)
                    .addMetadata("count", count);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
