package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * Monitors player health and sends low health events.
 * Triggers when health drops below 30% (6.0 HP out of 20.0).
 */
public class HealthMonitor {
    private static final float LOW_HEALTH_THRESHOLD = 0.3f; // 30% of max health
    private static final String EVENT_KEY = "low_health";

    private boolean wasLowHealth = false;

    public void onClientTick(Minecraft client) {
        if (client.player == null) {
            wasLowHealth = false;
            return;
        }

        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        float healthPercent = health / maxHealth;

        // Health <= 0 means player is dead, not "low health"
        // Suppress to avoid sending misleading events (PlayerDeath handles death)
        boolean isLowHealth = health > 0 && healthPercent < LOW_HEALTH_THRESHOLD;

        // Only trigger once when health first drops below threshold
        if (isLowHealth && !wasLowHealth) {
            EventPacket packet = new EventPacket("low_health")
                    .addMetadata("health", Math.round(health * 10.0) / 10.0)
                    .addMetadata("maxHealth", Math.round(maxHealth * 10.0) / 10.0)
                    .addMetadata("healthPercent", Math.round(healthPercent * 100));

            NetworkManager.getInstance().sendEvent(packet);
        }

        wasLowHealth = isLowHealth;
    }
}
