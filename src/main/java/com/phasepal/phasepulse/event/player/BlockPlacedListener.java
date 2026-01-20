package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;

/**
 * Listens for block placed events using Fabric API.
 */
public class BlockPlacedListener {
    private final EventDebouncer debouncer = new EventDebouncer();

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() && player.getStackInHand(hand).getItem() instanceof net.minecraft.item.BlockItem) {
                String blockType = player.getStackInHand(hand).getItem().toString();

                if (debouncer.shouldTrigger("block_placed", 100)) {
                    EventPacket packet = new EventPacket("block_placed")
                            .addMetadata("block", blockType);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
            return ActionResult.PASS;
        });
    }
}
