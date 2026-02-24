package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;

/**
 * Tracks combat state using damage events.
 * State machine: IDLE -> IN_COMBAT (on damage) -> IDLE (5 seconds no damage)
 */
public class CombatTracker {
    private static final long COMBAT_TIMEOUT_MS = 5000; // 5 seconds
    private static final EventDebouncer debouncer = new EventDebouncer();

    private boolean inCombat = false;
    private long combatStartTime = 0;
    private long lastDamageTime = 0;

    public void register() {
        // Combat tracking is handled via DamageMixin calling onPlayerDamaged()
        // No event registration needed here
    }

    /**
     * Called when the player takes damage or performs an attack.
     * Sustains the "in combat" state and updates the last activity timestamp.
     */
    public void onCombatActivity() {
        long now = System.currentTimeMillis();

        if (!inCombat) {
            // Entering combat
            inCombat = true;
            combatStartTime = now;
            lastDamageTime = now;

            if (debouncer.shouldTrigger("combat_start")) {
                EventPacket packet = new EventPacket("combat_start");
                NetworkManager.getInstance().sendEvent(packet);
            }
        } else {
            // Already in combat, update last activity time
            lastDamageTime = now;
        }
    }

    /**
     * Called every client tick to check if combat has ended.
     */
    public void onClientTick() {
        if (!inCombat) {
            return;
        }

        long now = System.currentTimeMillis();

        // If hostile mobs are still nearby, keep the combat state alive
        if (HostileMobDetector.areHostilesNearby()) {
            lastDamageTime = now;
        }

        long timeSinceLastActivity = now - lastDamageTime;

        if (timeSinceLastActivity >= COMBAT_TIMEOUT_MS) {
            // Combat ended (no damage taken/dealt and no hostiles nearby for 5s)
            inCombat = false;
            long durationSeconds = (now - combatStartTime) / 1000;

            if (debouncer.shouldTrigger("combat_end")) {
                EventPacket packet = new EventPacket("combat_end")
                        .addMetadata("duration_seconds", durationSeconds);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
