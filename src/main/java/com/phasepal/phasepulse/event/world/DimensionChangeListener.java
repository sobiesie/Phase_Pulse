package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * Tracks dimension changes (Overworld, Nether, End).
 */
public class DimensionChangeListener {
    private final EventDebouncer debouncer = new EventDebouncer();
    private String lastDimension = null;

    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }

        String currentDimension = client.level.dimension().identifier().toString();

        if (lastDimension == null) {
            lastDimension = currentDimension;
            return;
        }

        if (!currentDimension.equals(lastDimension)) {
            String from = lastDimension;
            lastDimension = currentDimension;

            if (debouncer.shouldTrigger("dimension_changed", 5000)) {
                EventPacket packet = new EventPacket("dimension_changed")
                        .addMetadata("from", from)
                        .addMetadata("to", currentDimension);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
