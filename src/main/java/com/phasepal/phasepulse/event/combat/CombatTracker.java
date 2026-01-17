package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

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
        // Track when player takes damage to detect combat
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // We'll use a client-side approach instead via mixin or tick
                // For now, we'll handle this in a different way
            }
        });
    }

    /**
     * Called when the player takes damage.
     * Should be called from client tick or damage event.
     */
    public void onPlayerDamaged() {
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
            // Already in combat, update last damage time
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
        long timeSinceLastDamage = now - lastDamageTime;

        if (timeSinceLastDamage >= COMBAT_TIMEOUT_MS) {
            // Combat ended
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
