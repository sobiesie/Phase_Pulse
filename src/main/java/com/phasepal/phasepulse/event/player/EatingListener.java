package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Listens for player eating/consuming items.
 * Called from EatingMixin injection.
 */
public class EatingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when a living entity consumes an item.
     * This is invoked from the EatingMixin.
     * @param entity The entity consuming the item
     * @param stack The item being consumed
     */
    public static void onItemConsumed(LivingEntity entity, ItemStack stack) {
        // Only track client-side player eating
        if (!(entity instanceof PlayerEntity) || !entity.getEntityWorld().isClient()) {
            return;
        }

        if (debouncer.shouldTrigger("eating")) {
            String itemName = stack.getItem().toString();
            EventPacket packet = new EventPacket("item_consumed")
                    .addMetadata("item", itemName);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
