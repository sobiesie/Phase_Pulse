package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors player air level (drowning detection).
 * Triggers when player is running out of air underwater.
 */
public class DrowningMonitor {
    private static final String EVENT_KEY = "drowning";
    private static final int DROWNING_THRESHOLD = 100; // Less than 5 seconds of air (300 max air)

    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasDrowning = false;

    public void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        int air = client.player.getAir();
        int maxAir = client.player.getMaxAir();

        boolean isDrowning = air < DROWNING_THRESHOLD && air < maxAir;

        // Trigger on transition to drowning state, or periodically while drowning
        if (isDrowning && (!wasDrowning || debouncer.shouldTrigger(EVENT_KEY, 5000))) {
            EventPacket packet = new EventPacket("drowning")
                    .addMetadata("air", air)
                    .addMetadata("maxAir", maxAir);

            NetworkManager.getInstance().sendEvent(packet);
        }

        wasDrowning = isDrowning;
    }
}
