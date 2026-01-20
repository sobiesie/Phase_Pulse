package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

/**
 * Listens for block broken events using Fabric API.
 */
public class BlockBrokenListener {
    private final EventDebouncer debouncer = new EventDebouncer();

    public void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) {
                String blockType = state.getBlock().toString();

                if (debouncer.shouldTrigger("block_broken", 100)) {
                    EventPacket packet = new EventPacket("block_broken")
                            .addMetadata("block", blockType);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
        });
    }
}
