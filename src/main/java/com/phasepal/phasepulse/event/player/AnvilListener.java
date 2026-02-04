package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;

/**
 * Listens for anvil usage events.
 * Called from AnvilMixin when player takes output from anvil.
 */
public class AnvilListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when player takes an item from anvil output.
     * @param result The resulting item from the anvil
     */
    public static void onAnvilUsed(ItemStack result) {
        if (result == null || result.isEmpty()) {
            return;
        }

        String itemType = result.getItem().toString();

        if (debouncer.shouldTrigger("anvil_used", 500)) {
            EventPacket packet = new EventPacket("anvil_used")
                    .addMetadata("result_item", itemType);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
