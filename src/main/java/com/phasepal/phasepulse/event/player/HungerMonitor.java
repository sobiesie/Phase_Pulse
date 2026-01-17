package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors player hunger and sends low hunger events.
 * Triggers when hunger drops below 3 bars (6 hunger points out of 20).
 */
public class HungerMonitor {
    private static final int LOW_HUNGER_THRESHOLD = 6; // 3 bars
    private static final String EVENT_KEY = "low_hunger";

    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasLowHunger = false;

    public void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        int hunger = client.player.getHungerManager().getFoodLevel();
        float saturation = client.player.getHungerManager().getSaturationLevel();

        boolean isLowHunger = hunger < LOW_HUNGER_THRESHOLD;

        // Trigger on transition to low hunger, or periodically while low
        if (isLowHunger && (!wasLowHunger || debouncer.shouldTrigger(EVENT_KEY, 10000))) {
            EventPacket packet = new EventPacket("low_hunger")
                    .addMetadata("hunger", hunger)
                    .addMetadata("saturation", Math.round(saturation * 10.0) / 10.0);

            NetworkManager.getInstance().sendEvent(packet);
        }

        wasLowHunger = isLowHunger;
    }
}
