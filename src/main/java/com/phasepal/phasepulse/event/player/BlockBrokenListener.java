package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Listens for block broken events on the client side.
 * Uses BlockBreakMixin to detect when player breaks blocks.
 */
public class BlockBrokenListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called from BlockBreakMixin when the player breaks a block.
     */
    public static void onBlockBroken(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        BlockState state = client.world.getBlockState(pos);
        String blockType = state.getBlock().toString();

        if (debouncer.shouldTrigger("block_broken", 100)) {
            EventPacket packet = new EventPacket("block_broken")
                    .addMetadata("block", blockType);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
