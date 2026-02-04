package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;

/**
 * Listens for player death events on the client side.
 * Detects death by monitoring the player's isDead() state transition.
 */
public class DeathListener {
    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasDead = false;

    public void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            wasDead = false;
            return;
        }

        boolean isDead = client.player.isDead();

        // Detect transition from alive to dead
        if (isDead && !wasDead) {
            if (debouncer.shouldTrigger("player_death")) {
                String deathCause = "unknown";

                DamageSource recentDamage = client.player.getRecentDamageSource();
                if (recentDamage != null) {
                    deathCause = recentDamage.getName();
                }

                EventPacket packet = new EventPacket("player_death")
                        .addMetadata("cause", deathCause);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }

        wasDead = isDead;
    }
}
