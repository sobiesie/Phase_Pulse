package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors player health and sends low health events.
 * Triggers when health drops below 30% (6.0 HP out of 20.0).
 */
public class HealthMonitor {
    private static final float LOW_HEALTH_THRESHOLD = 0.3f; // 30% of max health
    private static final String EVENT_KEY = "low_health";

    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasLowHealth = false;

    public void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        float healthPercent = health / maxHealth;

        boolean isLowHealth = healthPercent < LOW_HEALTH_THRESHOLD;

        // Trigger on transition to low health, or periodically while low
        if (isLowHealth && (!wasLowHealth || debouncer.shouldTrigger(EVENT_KEY, 10000))) {
            EventPacket packet = new EventPacket("low_health")
                    .addMetadata("health", Math.round(health * 10.0) / 10.0)
                    .addMetadata("maxHealth", Math.round(maxHealth * 10.0) / 10.0)
                    .addMetadata("healthPercent", Math.round(healthPercent * 100));

            NetworkManager.getInstance().sendEvent(packet);
        }

        wasLowHealth = isLowHealth;
    }
}
