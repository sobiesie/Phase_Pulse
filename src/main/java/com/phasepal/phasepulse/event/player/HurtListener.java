package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import com.phasepal.phasepulse.event.combat.HostileMobDetector;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;

/**
 * Monitors player hurt state via client tick.
 * Detects damage by tracking when hurtTime becomes > 0 (red flash animation).
 * Also tracks health changes to estimate damage amount.
 */
public class HurtListener {
    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasHurt = false;
    private float lastHealth = -1;

    /**
     * Called every client tick to check for hurt state.
     */
    public void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        float currentHealth = client.player.getHealth();
        boolean isHurt = client.player.hurtTime > 0;

        // Detect transition from not-hurt to hurt
        if (isHurt && !wasHurt) {
            // Calculate damage from health difference
            float damage = 0;
            if (lastHealth > 0 && currentHealth < lastHealth) {
                damage = lastHealth - currentHealth;
            }

            if (debouncer.shouldTrigger("player_hurt", 1000)) { // 1 second debounce
                String damageSource = "unknown";
                DamageSource recentDamage = client.player.getRecentDamageSource();
                if (recentDamage != null) {
                    damageSource = recentDamage.getName();
                }

                EventPacket packet = new EventPacket("player_hurt")
                        .addMetadata("damage", Math.round(damage * 10.0) / 10.0)
                        .addMetadata("source", damageSource);

                NetworkManager.getInstance().sendEvent(packet);
            }

            // Only trigger combat if hostile mobs are nearby (not for fall/environmental damage)
            if (HostileMobDetector.areHostilesNearby()) {
                CombatTracker tracker = EventRegistry.getCombatTracker();
                if (tracker != null) {
                    tracker.onCombatActivity();
                }
            }
        }

        wasHurt = isHurt;
        lastHealth = currentHealth;
    }
}
