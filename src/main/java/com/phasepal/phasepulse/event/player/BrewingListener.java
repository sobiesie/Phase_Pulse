package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

/**
 * Handles brewing events.
 */
public class BrewingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when a player completes brewing a potion.
     * @param potionStack The potion brewed
     */
    public static void onPotionBrewed(ItemStack potionStack) {
        String potionId = Registry.ITEM.getId(potionStack.getItem()).toString().replace("minecraft:", "");
        int count = potionStack.getCount();

        if (debouncer.shouldTrigger("potion_brewed", 500)) {
            EventPacket packet = new EventPacket("potion_brewed")
                    .addMetadata("potion", potionId)
                    .addMetadata("count", count);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
