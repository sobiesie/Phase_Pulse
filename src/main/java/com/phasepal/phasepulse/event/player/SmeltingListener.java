package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;

/**
 * Listens for item smelted events.
 * Called from SmeltingMixin when player takes output from furnace/blast furnace/smoker.
 */
public class SmeltingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when player takes a smelted item from furnace output.
     * @param stack The item that was smelted
     */
    public static void onItemSmelted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemType = stack.getItem().toString();
        int count = stack.getCount();

        if (debouncer.shouldTrigger("smelt_" + itemType, 500)) {
            EventPacket packet = new EventPacket("item_smelted")
                    .addMetadata("item", itemType)
                    .addMetadata("count", count);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
